import operator

import numpy
import scipy

from division import Division

for number_of_matches in range(1, 16, 2):
    main = Division(name="Main", team_list=None, no_of_teams=16, skill_style=2)
    main.swiss_run_matches(number_of_matches)
    team_names = [team.team.name for team in main.team_list]
    team_skills = [team.team.skill for team in main.team_list]
    team_match_points = [team.match_points for team in main.team_list]
    team_skill_rank = [sorted(team_skills).index(team) for team in team_skills]
    team_match_point_rank = [sorted(team_match_points).index(team) for team in team_match_points]
    distortions = list(map(operator.sub, team_skill_rank, team_match_point_rank))
    print(f"Swiss 16-team Main with {str(number_of_matches)} matches has total distortion magnitude (/2) of {sum([abs(distortion) for distortion in distortions])}")

main = Division(name="Main", team_list=None, no_of_teams=16, skill_style=2)
main.rr_run_matches()
team_names = [team.team.name for team in main.team_list]
team_skills = [team.team.skill for team in main.team_list]
team_match_points = [team.match_points for team in main.team_list]
team_skill_rank = [sorted(team_skills).index(team) for team in team_skills]
team_match_point_rank = [sorted(team_match_points).index(team) for team in team_match_points]
distortions = list(map(operator.sub, team_skill_rank, team_match_point_rank))
print(f"RR 16-team Main with 15 matches has total distortion magnitude (/2) of {sum([abs(distortion) for distortion in distortions])}")