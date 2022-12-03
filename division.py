from numpy import random

from team import Team, TeamContext, ByeTeam
from tfmatch import Match


class Division:
    def __init__(self, name: str, team_list: list or None, no_of_teams: int or None, skill_style: int or None):
        """
        This constructor is used for divs that are already final, not simulated, e.g. existing divs
        :param name: The name of the team
        :param team_list: list of TeamContexts; pass None unless you have a premade list
        :param no_of_teams: number of teams to create
        :param skill_style: way skill should be distributed - 0 for all teams have same skill, 1 for evenly distributed
        from -3 to 3, 2 for normally distributed
        """

        # Initializes values from parameters
        self.name = name
        if team_list is not None:
            self.team_list = team_list
            return
        else:
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

        # Add bye week for odd number of teams
        if len(self.team_list) % 2 == 1:
            self.team_list.append(TeamContext(ByeTeam()))

    def rr_run_matches(self):
        week_list = []

        # Generate matchups
        # Circle method for generating matchups
        # https://en.wikipedia.org/wiki/Round-robin_tournament#Circle_method
        fixed_team = self.team_list[0]
        rotating_teams = self.team_list[1:]

        number_teams = len(self.team_list)

        for i in range(number_teams - 1):
            week = []
            current_team_list = rotating_teams.copy()
            current_team_list.insert(0, fixed_team)
            for j in range(0, round(number_teams / 2)):
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
            # print(printed)

    # DFS based algorithm to run Swiss matches
    def swiss_run_matches(self, number_of_matches: int):

        # Play the week's matches and make necessary adjustments for every week
        for week_no in range(number_of_matches):
            teams_sorted = self.team_list.copy()
            teams_sorted.sort(reverse=True)
            # print("============================================================")
            # print("Creating schedule for week " + str(week_no))
            # print("============================================================")
            schedule_unpacked = self.schedule_week([], teams_sorted, True)

            # Pack schedule
            schedule = []
            schedule_iter = iter(schedule_unpacked)
            while True:
                try:
                    schedule.append((next(schedule_iter), next(schedule_iter)))
                except StopIteration:
                    break

            printed = "Week " + str(week_no) + ": " + str(schedule)

            # Run the matches
            for pairing in schedule:
                my_match = Match(pairing[0].team.skill, pairing[1].team.skill, week_no % 2 != 0)
                pairing[0] + my_match.get_home_result()
                pairing[1] + my_match.get_away_result()
                pairing[0].teams_faced.append(pairing[1])
                pairing[1].teams_faced.append(pairing[0])
                printed += " " + str(my_match)

            # Sort teams_sorted
            teams_sorted = self.team_list.copy()
            teams_sorted.sort(reverse=True)

            printed += "\nName,Skill,W,L,RW,RL,MP\n"
            for team in teams_sorted:
                printed += str(team) + " Already faced " + str(team.teams_faced) + "\n"

            # print(printed)

    # Recursive algorithm to schedule a given week's matches. Refer to
    # https://stackoverflow.com/questions/70236750/tree-recursion-how-to-include-conditions-in-depth-first-search
    def schedule_week(self, s: list, r: list, home: bool):
        # Tree of team pairs, depth-first traversal with red and green nodes
        # Use pairs to determine what is a red node.
        # - If either team is already in the schedule for this week, it's red.
        # -- We're not using a proper schedule (list of tuples) here so don't use team_search_in_schedule
        # - If either team has already played the other, it's red.
        # -- Can use Team.teams_faced to determine this
        # Do not visit red nodes (do not add them to schedule) and do not traverse their children
        # Visit green nodes but REVERSIBLY - We want to be able to backtrack if we don't reach a green leaf!
        # In effect, we're building an ordered list where even indices (starting 0) are home & the following odd
        # indices (starting 1) are away to them - 1. This means we perform additional checks for redness if home False
        schedule = s.copy()
        remaining_teams = r.copy()
        # Base case 1: Negative (red node)
        # Is the team already in the schedule?
        if remaining_teams[0] in schedule:
            return None
        # If away, has the team played home already in past weeks?
        if not home and remaining_teams[0] in schedule[-1].teams_faced:
            return None
        # Base case 2: Positive (reached [green] leaf) - should only happen when home is False (i.e. team is away)
        if not remaining_teams[1:]:
            if home:
                raise Exception("Odd number of teams!")
            schedule.append(remaining_teams.pop(0))
            return schedule
        # Recursive step
        for team in remaining_teams[1:]:
            arg_remaining_teams = remaining_teams[1:]
            arg_remaining_teams.remove(team)
            arg_remaining_teams.insert(0, team)
            path = self.schedule_week(schedule + [remaining_teams[0]], arg_remaining_teams, not home)
            if path is not None:
                return path
        # No path found
        return None

    def __str__(self):
        teams_sorted = self.team_list.copy()
        teams_sorted.sort(reverse=True)
        returned = "Name,Skill,W,L,RW,RL,MP\n"
        for team in teams_sorted:
            returned += str(team) + "\n"
        return returned


def team_search_in_schedule(schedule: list, searched: TeamContext):
    for matchup in schedule:
        for team in matchup:
            if team == searched:
                return True
    return False
