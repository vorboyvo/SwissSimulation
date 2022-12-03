import numpy
import scipy

from division import Division

for number_of_matches in range(1, 16, 2):
    main = Division(name="Main", team_list=None, no_of_teams=16, skill_style=1)
    main.swiss_run_matches(number_of_matches)
    print(f"Swiss 16-team Main with {str(number_of_matches)} matches:")
    team_names = [team.team.name for team in main.team_list]
    team_skills = [team.team.skill for team in main.team_list]
    team_match_points_normalized = [team.match_points / number_of_matches for team in main.team_list]
    y = numpy.array(team_match_points_normalized)
    x = numpy.array(team_skills)
    reg = scipy.stats.linregress(x, y)
    print(reg.slope)

main = Division(name="Main", team_list=None, no_of_teams=16, skill_style=1)
main.rr_run_matches()
print("RR 16-team Main with 15 matches:")
team_names = [team.team.name for team in main.team_list]
team_skills = [team.team.skill for team in main.team_list]
team_match_points_normalized = [team.match_points / 15 for team in main.team_list]
y = numpy.array(team_match_points_normalized)
x = numpy.array(team_skills)
reg = scipy.stats.linregress(x, y)
print(reg.slope)
