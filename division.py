from numpy import random

from team import Team, TeamContext
from tfmatch import Match


class Division:
    def __init__(self, name: str, team_list: list or None, no_of_teams: int or None, skill_style: int or None):
        """
        This constructor is used for divs that are already final, not simulated, e.g. existing divs
        :param name: The name of the team
        :param team_list: list of TeamContexts
        """
        self.name = name
        if team_list is not None:
            self.team_list = team_list
            return

        # Initializes values from parameters
        self.name = name
        self.team_list = []

        # Creates teams according to skill style
        generator = random.default_rng()

        # Skill style 0: teams even
        if skill_style == 0:
            for i in range(0, no_of_teams):
                self.team_list.append(TeamContext(Team(name="Team " + str(i), skill=0)))
        # Skill style 1: teams equally spaced out
        elif skill_style == 1:
            for i in range(0, no_of_teams):
                skill = ((i / (no_of_teams - 1)) * 6) - 3
                self.team_list.append(TeamContext(Team(name="Team " + str(i), skill=skill)))
        # Skill style 2: Skills generated according to normal distribution
        elif skill_style == 2:
            skill_list = generator.normal(0, 1, no_of_teams)
            for i in range(0, no_of_teams):
                self.team_list.append(TeamContext(Team(name="Team " + str(i), skill=skill_list[i])))
        else:
            raise Exception("Skill style invalid: " + str(skill_style))

    def rr_run_matches(self):
        week_list = []

        # Generate matchups
        # Circle method for generating matchups
        # https://en.wikipedia.org/wiki/Round-robin_tournament#Circle_method
        fixed_team = self.team_list[0]
        rotating_teams = self.team_list[1:8]

        for i in range(7):
            week = []
            current_team_list = rotating_teams.copy()
            current_team_list.insert(0, fixed_team)
            for j in range(0, 4):  # look I have a reason ok
                week.append((current_team_list[j], current_team_list[-j - 1]))
            temp = rotating_teams[0]
            rotating_teams.remove(temp)
            rotating_teams.append(temp)
            #print("Week " + str(i) + ": " + str(week))
            week_list.append(week)

        # Run matches
        for week in week_list:
            printed = "Week " + str(week_list.index(week)) + ": " + str(week)
            if week_list.index(week) % 2 == 0:
                koth = False
            else:
                koth = True
            for pairing in week:
                my_match = Match(pairing[0].team.skill, pairing[1].team.skill, koth)
                pairing[0] + my_match.get_home_result()
                pairing[1] + my_match.get_away_result()
                printed += " " + str(my_match)
            print(printed)

    def __str__(self):
        returned = ""
        for team in self.team_list:
            returned += str(team) + "\n"
        return returned