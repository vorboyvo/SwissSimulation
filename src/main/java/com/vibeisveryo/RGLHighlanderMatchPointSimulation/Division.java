package com.vibeisveryo.RGLHighlanderMatchPointSimulation;

import java.util.*;

public class Division {
    private final String name;
    private final List<TeamContext> teamList; //Should always be sorted if any teams are modified!
    private final int verbose;
    private final long seed;

    /**
     * This constructor is used for divs that are already final, not simulated, e.g. existing divs.
     *
     * @param name The name of the division
     * @param teamList list of TeamContexts; pass null unless you have a premade list
     * @param noOfTeams number of teams to create
     * @param skillStyle way skill should be distributed - 0 for all teams have same skill, 1 for evenly distributed
     *                   from -3 to 3, 2 for normally distributed
     * @param verbose Verbosity level; 0 silent, 1 basic, 2 detailed
     * @param seed Seed to use for RNG if deterministic results desired; use -1L if we want random
     */
    public Division(String name, List<TeamContext> teamList, int noOfTeams, int skillStyle,
                    int verbose, Long seed) {
        this.name = name;
        if (teamList != null) {
            this.teamList = teamList;
            this.verbose = verbose;
            this.seed = seed;
            this.teamList.sort(Collections.reverseOrder());
            return;
        } else {
            this.teamList = new ArrayList<>();
        }
        this.verbose = verbose;
        this.seed = seed;

        // Creates teams according to skill style
        Random generator = (seed == -1) ? new Random() : new Random(seed);
        // Skill style 0: teams even
        if (skillStyle == 0) {
            for (int i = 0; i < noOfTeams; i++) {
                this.teamList.add(new TeamContext(new Team("com.vibeisveryo.RGLHighlanderMatchPointSimulation.Team " + i, 0)));
            }
        // Skill style 1: teams equally spaced out
        } else if (skillStyle == 1) {
            for (int i = 0; i < noOfTeams; i++) {
                double skill = ((double) i / (noOfTeams - 1)) * 6 - 3;
                this.teamList.add(new TeamContext(new Team("com.vibeisveryo.RGLHighlanderMatchPointSimulation.Team " + i, skill)));
            }
        // Skill style 2: Skills generated according to normal distribution
        } else if (skillStyle == 2) {
            for (int i = 0; i < noOfTeams; i++) {
                double skill = generator.nextGaussian();
                this.teamList.add(new TeamContext(new Team("com.vibeisveryo.RGLHighlanderMatchPointSimulation.Team " + i, skill)));
            }
        } else {
            throw new IllegalArgumentException("Skill style invalid: " + skillStyle);
        }

        Collections.shuffle(this.teamList);

        // Add bye week for odd number of teams
        if (this.teamList.size() % 2 == 1) {
            this.teamList.add(new TeamContext(new ByeTeam()));
        }
    }

    public Match runMatch(TeamContext homeTeam, TeamContext awayTeam, boolean koth) {
        Match myMatch = new Match(homeTeam.getTeam().getSkill(), awayTeam.getTeam().getSkill(), koth, seed);
        homeTeam.addMatch(myMatch.getHomeResult());
        awayTeam.addMatch(myMatch.getAwayResult());
        homeTeam.getTeamsFaced().add(awayTeam);
        awayTeam.getTeamsFaced().add(homeTeam);
        return myMatch;
    }

    /**
     * Run RR matches for this div. No parameters, the # of matches is fixed to
     * div size - 1.
     */
    public void rrRunMatches() {
        List<List<TeamContext[]>> weekList = new ArrayList<>();

        // Generate matchups
        // Circle method for generating matchups
        // https://en.wikipedia.org/wiki/Round-robin_tournament#Circle_method
        int numberOfTeams = this.teamList.size();
        TeamContext fixedTeam = this.teamList.get(0);
        List<TeamContext> rotatingTeams = this.teamList.subList(1,numberOfTeams);

        for (int i = 0; i < numberOfTeams - 1; i++) {
            List<TeamContext[]> week = new ArrayList<>();
            List<TeamContext> currentTeamList = new ArrayList<>(rotatingTeams);
            currentTeamList.add(0, fixedTeam);
            for (int j = 0; j < Math.round(numberOfTeams / 2.0); j++) {
                week.add(new TeamContext[]{currentTeamList.get(j),
                        currentTeamList.get(currentTeamList.size() - j - 1)
                });
            }
            Collections.rotate(rotatingTeams, -1);
            weekList.add(week);
        }

        // Run matches
        int i = 0;
        for (List<TeamContext[]> week: weekList) {
            // StringBuilder printed = new StringBuilder();
            // printed.append("Week ").append(i).append(": ").append(week);
            boolean koth = i % 2 != 0;
            for (TeamContext[] pairing: week) {
                Match match = this.runMatch(pairing[0], pairing[1], koth);
                // printed.append(" ").append(match);
            }
            i++;
            // System.out.println(printed);
        }

        // Sort team list
        this.teamList.sort(Collections.reverseOrder());
    }

    /**
     * Runs matches for a swiss season.
     * Returns skill diffs WITHIN A SINGLE SEASON if needed; wrapped and overloaded by method that takes just one int
     * argument.
     * @param matchCount Number of matches to be played
     * @param getSkill true if we want skill, false if we don't care
     * @return int[matchCount][# of matches in a week] with skill diffs, but null if not getSkill
     */
    public double[][] swissRunMatches(int matchCount, boolean getSkill) {
        // If we don't care about skill diffs, just run matches and return null
        double[][] skillDiffs;
        if (getSkill) skillDiffs = new double[matchCount][this.teamList.size()/2];
        else skillDiffs = null;

        // Play the week's matches and make necessary adjustments for each week
        for (int weekNo = 0; weekNo < matchCount; weekNo++) {
            // Schedule matches
            List<TeamContext[]> schedule = this.scheduleWeek();

            // Run matches and get skill diffs
            int i = 0;
            for (TeamContext[] pairing: schedule) {
                if (getSkill) skillDiffs[weekNo][i] = Math.abs(
                        pairing[0].getTeam().getSkill() - pairing[1].getTeam().getSkill()
                );
                this.runMatch(pairing[0], pairing[1], weekNo % 2 != 0);
            }

            // Sort team list
            this.teamList.sort(Collections.reverseOrder());
        }
        return skillDiffs;
    }

    /**
     * Runs matches for a Swiss season.
     * Overloaded by swissRunMatches(int, boolean) - use that with true to get skill diffs.
     * @param matchCount Number of matches to be played
     */
    public void swissRunMatches(int matchCount) {
        swissRunMatches(matchCount, false);
    }

    /**
     * Run a single week's matches. Wraps a recursive method that uses DFS to find a working set of matches.
     * @return List of pairs of com.vibeisveryo.RGLHighlanderMatchPointSimulation.TeamContext for that week's matches, where the pair's index 0 is home and 1 is away.
     */
    private List<TeamContext[]> scheduleWeek() {
        // Assume team list is sorted. Otherwise, we have other issues going on.
        TeamContext[] scheduleUnpacked = dfsFindSchedule(new TeamContext[0], this.teamList.toArray(new TeamContext[0]),
                true, 0, null);
        if (scheduleUnpacked == null) throw new NullPointerException("Could not find a valid set of matches!");
        // Pack schedule into list of pairs
        List<TeamContext[]> schedule = new ArrayList<>(scheduleUnpacked.length/2);
        Iterator<TeamContext> packer = Arrays.stream(scheduleUnpacked).iterator();
        while (packer.hasNext()) {
            TeamContext[] pairing = {packer.next(), packer.next()};
            schedule.add(pairing);
        }
        return schedule;
    }

    /**
     * Recursive algorithm to find a (sub)schedule that works Swiss wise using DFS
     * @param schedule The schedule, as tried so far, expressed as a list to be interpreted as even-odd pairings
     * @param remainingTeams Teams left to be scheduled
     * @param home Whether we are looking to schedule a home team (new match) or away team (fill match)
     * @param depth Current depth down the tree
     * @param scheduleFirst Which team to attempt scheduling first
     * @return Full schedule, or null if none was found down this path
     */
    private TeamContext[] dfsFindSchedule(TeamContext[] schedule, TeamContext[] remainingTeams, boolean home,
                                          int depth, TeamContext scheduleFirst
    ) {
        /*
         * Tree of team pairs, depth-first traversal with red and green nodes
         * Use pairs to determine what is a red node.
         * - If either team is already in the schedule for this week, it's red.
         * -- We're not using a proper schedule (list of tuples) here so don't use team_search_in_schedule
         * - If either team has already played the other, it's red.
         * -- Can use com.vibeisveryo.RGLHighlanderMatchPointSimulation.Team.teams_faced to determine this
         * Do not visit red nodes (do not add them to schedule) and do not traverse their children.
         * Visit green nodes but REVERSIBLY - We want to be able to backtrack if we don't reach a green leaf!
         * In effect, we're building an ordered array where even indices (starting 0) are home & the following odd
         * indices (starting 1) are away to their index - 1. So we do additional checks for redness if home False.
         */

        if (scheduleFirst == null) scheduleFirst = remainingTeams[0];

        // Base case: Positive (reached [green] leaf) - should only happen when home false (i.e. scheduling away team)
        if (remainingTeams.length == 1) {
            if (home) throw new RuntimeException("Odd number of teams!");
            // Append scheduled team to schedule and return
            TeamContext[] newSchedule = new TeamContext[schedule.length+1];
            newSchedule[schedule.length] = scheduleFirst;
            System.arraycopy(schedule, 0, newSchedule, 0, schedule.length);
            return newSchedule;
        }
        // Recursive case 1: On away -> Add current Away team to schedule, recur on rest
        if (!home) {
            // Append scheduled team to schedule
            TeamContext[] newSchedule = new TeamContext[schedule.length + 1];
            newSchedule[schedule.length] = scheduleFirst;
            System.arraycopy(schedule, 0, newSchedule, 0, schedule.length);
            // Recur on remainingTeams without scheduled team
            TeamContext[] newRemainingTeams = new TeamContext[remainingTeams.length-1];
            int j = 0;
            //  Copy remainingTeams to newRemainingTeams excluding newly scheduled team
            for (TeamContext temp : remainingTeams) {
                if (temp != scheduleFirst) {
                    newRemainingTeams[j] = temp;
                    j++;
                }
            }
            return this.dfsFindSchedule(newSchedule, newRemainingTeams, true, depth + 1, null);
        }
        // Recursive case 2: On home -> Look for an opponent to match home with by recurring, go down list of teams if
        // none found
        else {
            TeamContext[] newRemainingTeams = Arrays.copyOfRange(remainingTeams,1,remainingTeams.length);
            TeamContext homeTeam = remainingTeams[0];
            for (TeamContext team : newRemainingTeams) {
                if (homeTeam.getTeamsFaced().contains(team)) continue;
                // Append home team to schedule
                TeamContext[] newSchedule = new TeamContext[schedule.length + 1];
                newSchedule[schedule.length] = scheduleFirst;
                System.arraycopy(schedule, 0, newSchedule, 0, schedule.length);
                // Recur on newRemainingTeams
                TeamContext[] path = this.dfsFindSchedule(newSchedule, newRemainingTeams, false, depth + 1, team);
                if (path!=null) return path;
            }
        }
        // No path found
        return null;
    }

    /**
     * Returns this division as a multi-line String representation
     * @return In CSV format,
     */
    @Override
    public String toString() {
        StringBuilder returned = new StringBuilder(String.format("Division %s with %d teams\n", this.name,
                this.teamList.size()));
        returned.append("Name,Skill,W,L,RW,RL,MP,Teams Faced\n");
        for (TeamContext team: this.teamList) {
            returned.append(team.toString())
                    .append(',')
                    .append(team.getTeamsFacedNames())
                    .append('\n');
        }
        return returned.toString();
    }

    public List<TeamContext> getTeamList() {
        return teamList;
    }
}