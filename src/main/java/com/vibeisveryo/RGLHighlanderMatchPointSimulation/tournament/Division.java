package com.vibeisveryo.RGLHighlanderMatchPointSimulation.tournament;

import java.util.*;

public class Division {
    public enum VerbosityLevel {
        NONE(0),
        MINIMAL(1),
        DETAILED(2),
        FULL(3);
        public final int value;
        VerbosityLevel(int i) {
            value = i;
        }
    }

    public enum SkillStyle {
        IDENTICAL,
        UNIFORM,
        RANDOM_NORMAL,
        TRUE_RANDOM,
    }

    private final String name;
    private final List<Team> teamList; // Should always be sorted by any action modifying the team list!
    private final VerbosityLevel verbosityLevel; // TODO add functionality
    private final Random random;

    public Division(String name, int noOfTeams, SkillStyle skillStyle) {
        this.name = name;
        this.teamList = new ArrayList<>();
        this.verbosityLevel = VerbosityLevel.NONE;
        this.random = new Random();
        this.addTeams(noOfTeams, skillStyle);
    }

    public Division(String name, int noOfTeams, SkillStyle skillStyle, Long seed) {
        this.name = name;
        this.teamList = new ArrayList<>();
        this.verbosityLevel = VerbosityLevel.NONE;
        this.random = new Random(seed);
        this.addTeams(noOfTeams, skillStyle);
    }

    public Division(String name, List<Team> teamList) {
        this.name = name;
        this.teamList = teamList;
        this.verbosityLevel = VerbosityLevel.NONE;
        this.random = new Random();
    }

    public Division(String name, List<Team> teamList, Long seed) {
        this.name = name;
        this.teamList = teamList;
        this.verbosityLevel = VerbosityLevel.NONE;
        this.random = new Random(seed);
    }

    private Match runMatch(Team homeTeam, Team awayTeam, boolean koth) {
        Match myMatch;
        if (!homeTeam.isBye() && !awayTeam.isBye()) {
            myMatch = new Match(homeTeam.getTeam().getSkill(), awayTeam.getTeam().getSkill(), koth, this.random);
        } else if (homeTeam.isBye()) {
            myMatch = new Match(true);
        } else {
            myMatch = new Match(false);
        }
        homeTeam.addMatch(myMatch.getHomeResult());
        awayTeam.addMatch(myMatch.getAwayResult());
        homeTeam.getTeamsFaced().add(awayTeam);
        awayTeam.getTeamsFaced().add(homeTeam);
        return myMatch;
    }

    public Division addPreviousPair(Team... args) {
        if (args.length != 2) throw new IllegalArgumentException("Must have two arguments!");
        args[0].getTeamsFaced().add(args[1]);
        args[1].getTeamsFaced().add(args[0]);
        // Return this for convenience
        return this;
    }

    /**
     * Run RR matches for this div. No parameters, the # of matches is fixed to
     * div size - 1.
     */
    public void rrRunMatches() {
        List<List<Team[]>> weekList = new ArrayList<>();

        // Generate matchups
        // Circle method for generating matchups
        // https://en.wikipedia.org/wiki/Round-robin_tournament#Circle_method
        int numberOfTeams = this.teamList.size();
        Team fixedTeam = this.teamList.get(0);
        List<Team> rotatingTeams = this.teamList.subList(1,numberOfTeams);

        for (int i = 0; i < numberOfTeams - 1; i++) {
            List<Team[]> week = new ArrayList<>();
            List<Team> currentTeamList = new ArrayList<>(rotatingTeams);
            currentTeamList.add(0, fixedTeam);
            for (int j = 0; j < Math.round(numberOfTeams / 2.0); j++) {
                week.add(new Team[]{currentTeamList.get(j),
                        currentTeamList.get(currentTeamList.size() - j - 1)
                });
            }
            Collections.rotate(rotatingTeams, -1);
            weekList.add(week);
        }

        // Run matches
        int i = 0;
        for (List<Team[]> week: weekList) {
            // StringBuilder printed = new StringBuilder();
            // printed.append("Week ").append(i).append(": ").append(week);
            boolean koth = i % 2 != 0;
            for (Team[] pairing: week) {
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
     *
     */
    public List<Team[][]> randomRunMatches(int matchCount) {
        // Make list of matches
        List<Team[][]> matches = new ArrayList<>();
        // Play the week's matches and make necessary adjustments for each week
        for (int weekNo = 0; weekNo < matchCount; weekNo++) {

            // Randomize list order
            Team byeTeam = this.teamList.remove(this.teamList.size()-1);
            boolean byeRemoved = byeTeam.isBye();
            if (!byeRemoved) this.teamList.add(byeTeam);
            Collections.shuffle(this.teamList, this.random);
            if (byeRemoved) this.teamList.add(byeTeam);

            // Schedule matches by Swiss
            List<Team[]> schedule = this.scheduleWeek();
            matches.add(schedule.toArray(new Team[0][]));

            // Run matches
            for (Team[] pairing: schedule) {
                this.runMatch(pairing[0], pairing[1], weekNo % 2 != 0);
            }

            // Sort team list
            this.teamList.sort(Collections.reverseOrder());
        }
        return matches;
    }

    /**
     * Runs matches for a Swiss season.
     * To get skill diffs, don't use this - just use scheduleWeek and get the skill diffs from the returned schedule.
     * @param matchCount Number of matches to be played
     */
    public List<Team[][]> swissRunMatches(int matchCount) {
        // Make list of matches
        List<Team[][]> matches = new ArrayList<>();
        // Play the week's matches and make necessary adjustments for each week
        for (int weekNo = 0; weekNo < matchCount; weekNo++) {
            // Schedule matches
            List<Team[]> schedule = this.scheduleWeek();
            matches.add(schedule.toArray(new Team[0][]));

            // Run matches
            for (Team[] pairing: schedule) {
                this.runMatch(pairing[0], pairing[1], weekNo % 2 != 0);
            }

            // Sort team list
            this.teamList.sort(Collections.reverseOrder());
        }
        return matches;
    }

    /**
     * Run a single week's matches. Wraps a recursive method that uses DFS to find a working set of matches.
     *
     * @return List of pairs of TeamContext for that week's matches, where the pair's index 0 is home and 1 is away.
     */
    public List<Team[]> scheduleWeek() {
        // Assume team list is sorted. Otherwise, we have other issues going on.
        Deque<Team> scheduleUnpacked = dfsFindSchedule(new LinkedList<>(), this.teamList.toArray(new Team[0]),
                true, 0, null);
        if (scheduleUnpacked == null) throw new NullPointerException("Could not find a valid set of matches!");
        // Pack schedule into list of pairs
        List<Team[]> schedule = new ArrayList<>(scheduleUnpacked.size()/2);
        Iterator<Team> packer = scheduleUnpacked.iterator();
        while (packer.hasNext()) {
            Team[] pairing = {packer.next(), packer.next()};
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
    Deque<Team> dfsFindSchedule(Deque<Team> schedule, Team[] remainingTeams, boolean home,
                           int depth, Team scheduleFirst
    ) {

        if (scheduleFirst == null) scheduleFirst = remainingTeams[0];

        // Base case: Positive (reached [green] leaf) - should only happen when home false (i.e. scheduling away team)
        if (remainingTeams.length == 1) {
            if (home) throw new RuntimeException("Odd number of teams!");
            // Append scheduled team to schedule and return
            schedule.add(scheduleFirst);
            return schedule;
        }
        // Recursive case 1: On away -> Add current Away team to schedule, recur on rest
        if (!home) {
            // Append scheduled team to schedule
            schedule.add(scheduleFirst);
            // Recur on remainingTeams without scheduled team
            Team[] newRemainingTeams = new Team[remainingTeams.length-1];
            int j = 0;
            //  Copy remainingTeams to newRemainingTeams excluding newly scheduled team
            for (Team temp : remainingTeams) {
                if (temp != scheduleFirst) {
                    newRemainingTeams[j] = temp;
                    j++;
                }
            }
            return this.dfsFindSchedule(schedule, newRemainingTeams, true, depth + 1, null);
        }
        // Recursive case 2: On home -> Look for an opponent to match home with by recurring, go down list of teams if
        // none found
        else {
            // Append home team to schedule
            Team lastScheduled = schedule.peekLast();
            schedule.add(scheduleFirst);
            Team[] newRemainingTeams = Arrays.copyOfRange(remainingTeams,1,remainingTeams.length);
            Team homeTeam = remainingTeams[0];
            for (Team team : newRemainingTeams) {
                if (homeTeam.getTeamsFaced().contains(team)) continue;
                // Recur on newRemainingTeams
                Deque<Team> path = this.dfsFindSchedule(schedule, newRemainingTeams, false, depth + 1, team);
                if (path!=null) return path;
            }
            Team removed;
            do {
                removed = schedule.removeLast();
            } while (removed != lastScheduled);
            // No path found
            return null;
        }
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
        for (Team team: this.teamList) {
            returned.append(team.toString())
                    .append(',')
                    .append(team.getTeamsFacedNames())
                    .append('\n');
        }
        return returned.toString();
    }

    public List<Team> getTeamList() {
        return teamList;
    }

    private void addTeams(int noOfTeams, SkillStyle skillStyle) {
        if (skillStyle == SkillStyle.IDENTICAL) {
            for (int i = 0; i < noOfTeams; i++) {
                this.teamList.add(new Team("Team " + i, 0));
            }
            // Skill style 1: teams equally spaced out
        } else if (skillStyle == SkillStyle.UNIFORM) {
            for (int i = 0; i < noOfTeams; i++) {
                double skill = ((double) i / (noOfTeams - 1)) * 6 - 3;
                this.teamList.add(new Team("Team " + i, skill));
            }
            // Skill style 2: Skills generated according to normal distribution
        } else if (skillStyle == SkillStyle.RANDOM_NORMAL) {
            for (int i = 0; i < noOfTeams; i++) {
                double skill = this.random.nextGaussian();
                this.teamList.add(new Team("Team " + i, skill));
            }
        } else if (skillStyle == SkillStyle.TRUE_RANDOM) {
            for (int i = 0; i < noOfTeams; i++) {
                double skill = this.random.nextDouble(-3, 3);
                this.teamList.add(new Team("Team " + i, skill));
            }
        } else {
            throw new IllegalArgumentException("Skill style invalid: " + skillStyle);
        }

        Collections.shuffle(this.teamList, this.random);

        // Add bye week for odd number of teams, after shuffling so it's at the end
        // (It doesn't really make a huge difference but it's the principle of it)
        if (this.teamList.size() % 2 == 1) {
            this.teamList.add(new Team(true));
        }
    }
}