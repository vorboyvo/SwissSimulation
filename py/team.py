from tfmatch import MatchResult
from functools import total_ordering


@total_ordering
class Team:
    def __init__(self, name: str, skill: float, bye=False):
        """
        Initializes a team
        :param name:
        :param skill:
        """
        self.name = name
        self.skill = skill
        self.bye = bye

    def __lt__(self, other):
        return self.skill < other.skill

    def __eq__(self, other):
        return self.skill == other.skill


class ByeTeam(Team):
    def __init__(self):
        super().__init__("Bye Week", -99999999, True)


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
        self.teams_faced = []

    def __add__(self, other: MatchResult):
        self.wins += (1 if other.won else 0)
        self.losses += (0 if other.won else 1)
        self.rounds_won += other.rounds_won
        self.rounds_lost += other.rounds_lost
        self.match_points += other.match_points

    # Returns, tab separated: name, skill, wins, losses, rounds won, rounds lost, match points, inq match points
    def __str__(self):
        return self.team.name + "," + str(round(self.team.skill, 3)) + "," + str(self.wins) + "," + str(self.losses) \
               + "," + str(self.rounds_won) + "," + str(self.rounds_lost) + "," + str(self.match_points) + "," + str(self.teams_faced)

    def __repr__(self):
        return self.team.name

    def __lt__(self, other):
        if self.match_points < other.match_points:
            return True
        elif self.match_points > other.match_points:
            return False
        # Tiebreak by Median Buchholz
        self_median_buchholz = 0
        self_max_value = 0
        self_min_value = 999999  # Really Big Number
        for team in self.teams_faced:
            self_median_buchholz += team.match_points
            self_max_value = max(self_max_value, team.match_points)
            self_min_value = min(self_min_value, team.match_points)
        self_median_buchholz -= self_max_value
        self_median_buchholz -= self_min_value

        other_median_buchholz = 0
        other_max_value = 0
        other_min_value = 999999 # Really Big Number
        for team in other.teams_faced:
            other_median_buchholz += team.match_points
            other_max_value = max(other_max_value, team.match_points)
            other_min_value = min(other_min_value, team.match_points)
        other_median_buchholz -= other_max_value
        other_median_buchholz -= other_min_value

        if self_median_buchholz < other_median_buchholz:
            return True
        else:
            return False
