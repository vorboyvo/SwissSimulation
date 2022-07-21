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
            week_list.append(week)

        # Run matches
        for week in week_list:
            printed = "Week " + str(week_list.index(week)) + ": " + str(week)
            koth = week_list.index(week) % 2 != 0

            for pairing in week:
                my_match = Match(pairing[0].team.skill, pairing[1].team.skill, koth)
                pairing[0] + my_match.get_home_result()
                pairing[1] + my_match.get_away_result()
                printed += " " + str(my_match)
            #print(printed)

    def swiss_run_matches(self):
        teams_sorted = self.team_list.copy()
        # Run each week
        for week_no in range(7):

            print ("============================================================")
            print ("Creating schedule for week " + str(week_no))
            print("============================================================")
            # Create this week's schedule
            schedule = []

            for home_team in teams_sorted:

                print("------------------------------------------------------------")

                print("Attempting to add " + repr(home_team) + " to schedule!")

                print("Schedule so far: " + str(schedule))

                # Check if home team is already in schedule
                if team_search_in_schedule(schedule, home_team):
                    print("Home team " + repr(home_team) + " is already on the schedule")
                    continue

                for away_team in teams_sorted:
                    print("Checking if " + str((home_team,away_team)) + " is a valid pairing")
                    # Check if home is same as away
                    if home_team is away_team:
                        print("Home team and away team are identical")
                        continue

                    # Check if away team is already in schedule - if so, go to next eligible away team
                    if team_search_in_schedule(schedule, away_team):
                        print("Away team " + repr(away_team) + " is already on the schedule")
                        continue

                    # Check if home team has already played away team
                    if away_team in teams_faced[home_team.team.name]:
                        print(repr(home_team) + " has already played " + repr(away_team))
                        continue

                    schedule.append((home_team, away_team))
                    print("Added " + str((home_team,away_team)) + " to schedule!")
                    teams_faced[home_team.team.name].append(away_team)
                    teams_faced[away_team.team.name].append(home_team)
                    break
                if not team_search_in_schedule(schedule, home_team):
                    print("Failed to add " + repr(home_team) + " to schedule!")

            printed = "Week " + str(week_no) + ": " + str(schedule)

            # Run the matches
            for pairing in schedule:
                my_match = Match(pairing[0].team.skill, pairing[1].team.skill, week_no % 2 != 0)
                pairing[0] + my_match.get_home_result()
                pairing[1] + my_match.get_away_result()
                printed += " " + str(my_match)

            # Sort teams_sorted
            teams_sorted.sort(reverse=True)

            printed += "\nName,Skill,W,L,RW,RL,MP,IMP\n"
            for team in teams_sorted:
                printed += str(team) + " Already faced " + str(teams_faced[team.team.name]) + "\n"

            print(printed)

    def __str__(self):
        teams_sorted = self.team_list.copy()
        teams_sorted.sort(reverse=True)
        returned = "Name,Skill,W,L,RW,RL,MP,IMP\n"
        for team in teams_sorted:
            returned += str(team) + "\n"
        return returned

def team_search_in_schedule(schedule: list, searched: TeamContext):
    for matchup in schedule:
        for team in matchup:
            if team == searched:
                return True
    return False