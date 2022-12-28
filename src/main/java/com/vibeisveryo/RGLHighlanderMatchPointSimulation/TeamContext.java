package com.vibeisveryo.RGLHighlanderMatchPointSimulation;

import java.util.ArrayList;


public class TeamContext implements Comparable<TeamContext> {
    private Team team;
    private int wins;
    private int losses;
    private int roundsWon;
    private int roundsLost;
    private int matchPoints;
    private ArrayList<TeamContext> teamsFaced;

    public TeamContext(Team team) {
        this.team = team;
        this.wins = 0;
        this.losses = 0;
        this.roundsWon = 0;
        this.roundsLost = 0;
        this.matchPoints = 0;
        this.teamsFaced = new ArrayList<>();
    }

    public void addMatch(MatchResult matchResult) {
        this.wins += (matchResult.won() ? 1 : 0);
        this.losses += (matchResult.won() ? 0 : 1);
        this.roundsWon += matchResult.roundsWon();
        this.roundsLost += matchResult.roundsLost();
        this.matchPoints += matchResult.matchPoints();
    }

    public String toString() {
        return this.team.getName() + "," + String.format("%.3f", this.team.getSkill()) + "," + this.wins + "," + this.losses
                + "," + this.roundsWon + "," + this.roundsLost + "," + this.matchPoints;
    }

    public int compareTo(TeamContext other) {
        if (this.matchPoints < other.matchPoints) {
            return -1;
        } else if (this.matchPoints > other.matchPoints) {
            return 1;
        } else {
            // Tiebreak by Median Buchholz
            int thisMB = 0;
            int otherMB = 0;
            {
                int max = 0;
                int min = Integer.MAX_VALUE; // Really Big Number
                for (TeamContext team: this.teamsFaced) {
                    thisMB += team.matchPoints;
                    max = Math.max(max, team.matchPoints);
                    min = Math.min(min, team.matchPoints);
                }
                thisMB = thisMB - max - min;
            }
            {
                int max = 0;
                int min = Integer.MAX_VALUE; // Really Big Number
                for (TeamContext team: other.teamsFaced) {
                    otherMB += team.matchPoints;
                    max = Math.max(max, team.matchPoints);
                    min = Math.min(min, team.matchPoints);
                }
                otherMB = otherMB - max - min;
            }
            return thisMB - otherMB;
        }
    }

    public Team getTeam() {
        return team;
    }

    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }

    public int getRoundsWon() {
        return roundsWon;
    }

    public int getRoundsLost() {
        return roundsLost;
    }

    public int getMatchPoints() {
        return matchPoints;
    }

    public ArrayList<TeamContext> getTeamsFaced() {
        return teamsFaced;
    }

    public ArrayList<String> getTeamsFacedNames() {
        ArrayList<String> list = new ArrayList<>();
        for (TeamContext team: this.teamsFaced) {
            list.add(team.team.getName());
        }
        return list;
    }

    public void addTeamFaced(TeamContext team) {
        teamsFaced.add(team);
    }
}

class Team implements Comparable<Team> {
    private final String name;
    private final double skill;
    private final boolean bye;

    public Team(String name, double skill, boolean bye) {
        this.name = name;
        this.skill = skill;
        this.bye = bye;
    }

    public Team(String name, double skill) {
        this.name = name;
        this.skill = skill;
        this.bye = false;
    }

    public String getName() {
        return name;
    }

    public double getSkill() {
        return skill;
    }

    public boolean isBye() {
        return bye;
    }

    @Override
    public int compareTo(Team other) {
        return Double.compare(this.skill, other.skill);
    }
}

class ByeTeam extends Team {
    public ByeTeam() {
        super("Bye Week", -99999999, true);
    }
}

