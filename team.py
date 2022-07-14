from tfmatch import MatchResult
from functools import total_ordering

@total_ordering
class Team:
    def __init__(self, name: str, skill: float):
        """
        Initializes a team
        :param name:
        :param skill:
        """
        self.name = name
        self.skill = skill

    def __lt__(self, other):
        return self.skill < other.skill

    def __eq__(self, other):
        return self.skill == other.skill

@total_ordering
class TeamContext:
    def __init__(self, team: Team):
        """
        Gives a team its context in a division,
        :param team: the Team object
        """
        self.team = team
        self.wins = 0
        self.losses = 0
        self.rounds_won = 0
        self.rounds_lost = 0
        self.match_points = 0

    def __add__(self, other: MatchResult):
        self.wins += (1 if other.won else 0)
        self.losses += (0 if other.won else 1)
        self.rounds_won += other.rounds_won
        self.rounds_lost += other.rounds_lost
        self.match_points += other.match_points

    # Returns, tab separated: name, skill, wins, losses, rounds won, rounds lost, match points
    def __str__(self):
        return self.team.name + "," + str(round(self.team.skill,3)) + "," + str(self.wins) + "," + str(self.losses)\
               + "," + str(self.rounds_won) + "," + str(self.rounds_lost) + "," + str(self.match_points)

    def __repr__(self):
        return self.team.name

    def __lt__(self, other):
        try:
            if self.wins < other.wins:
                return True
            elif self.rounds_won / self.rounds_lost < other.rounds_won / other.rounds_lost:
                return True
            else:
                return False
        except ZeroDivisionError:
            if self.rounds_lost <= other.rounds_lost:
                return False
            else:
                return True

    def __eq__(self, other):
        try:
            return self.wins == other.wins and self.rounds_won/self.rounds_lost == other.rounds_won/other.rounds_lost
        except ZeroDivisionError:
            return True if self.rounds_lost == 0 and other.rounds_lost == 0 else False