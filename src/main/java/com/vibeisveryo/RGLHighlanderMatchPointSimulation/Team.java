package com.vibeisveryo.RGLHighlanderMatchPointSimulation;

import java.util.ArrayList;
import java.util.HashSet;


public class Team implements Comparable<Team> {
    private String name;
    private double skill;
    private boolean bye;


    private int wins;
    private int losses;
    private int roundsWon;
    private int roundsLost;
    private int matchPoints;
    private HashSet<Team> teamsFaced;

    public Team(String name, double skill) {
        this.name = name;
        this.skill = skill;
        this.bye = false;
        this.wins = 0;
        this.losses = 0;
        this.roundsWon = 0;
        this.roundsLost = 0;
        this.matchPoints = 0;
        this.teamsFaced = new HashSet<>();
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

    public void addMatch(MatchResult matchResult) {
        this.wins += (matchResult.won() ? 1 : 0);
        this.losses += (matchResult.won() ? 0 : 1);
        this.roundsWon += matchResult.roundsWon();
        this.roundsLost += matchResult.roundsLost();
        this.matchPoints += matchResult.matchPoints();
    }

    public String toString() {
        return this.getName() + "," + String.format("%.3f", this.getSkill()) + "," + this.wins + "," + this.losses
                + "," + this.roundsWon + "," + this.roundsLost + "," + this.matchPoints;
    }

    public int compareTo(Team other) {
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
                for (Team team: this.teamsFaced) {
                    thisMB += team.matchPoints;
                    max = Math.max(max, team.matchPoints);
                    min = Math.min(min, team.matchPoints);
                }
                thisMB = thisMB - max - min;
            }
            {
                int max = 0;
                int min = Integer.MAX_VALUE; // Really Big Number
                for (Team team: other.teamsFaced) {
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
        return this;
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

    public HashSet<Team> getTeamsFaced() {
        return teamsFaced;
    }

    public ArrayList<String> getTeamsFacedNames() {
        ArrayList<String> list = new ArrayList<>();
        for (Team team: this.teamsFaced) {
            list.add(team.getName());
        }
        return list;
    }

    public void addTeamFaced(Team team) {
        teamsFaced.add(team);
    }
}

class ByeTeam extends Team {
    boolean bye;
    public ByeTeam() {
        super("Bye Week", -99999999);
        this.bye = true;
    }
}

