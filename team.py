from tfmatch import MatchResult

class Team:
    def __init__(self, name: str, skill: float):
        """
        Initializes a team
        :param name:
        :param skill:
        """
        self.name = name
        self.skill = skill

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

    def __str__(self):
        return self.team.name + "\tSkill: " + str(round(self.team.skill,1)) + "\tW: " + str(self.wins) + "\tL: " + str(self.losses)\
               + "\tRW: " + str(self.rounds_won) + "\tRL: " + str(self.rounds_lost) + "\tMP: " + str(self.match_points)

    def __repr__(self):
        return self.team.name