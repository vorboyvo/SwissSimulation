import math
import operator
import random
import statistics
import time
from datetime import datetime

import pandas as pd
from scipy.stats import ttest_ind

from division import Division


def get_distortions(number_of_teams, number_of_matches=0):
    main = Division(name="Main", team_list=None, no_of_teams=number_of_teams, skill_style=2)
    random.shuffle(main.team_list)
    if number_of_matches == 0:
        main.rr_run_matches()
    else:
        main.swiss_run_matches(number_of_matches)
    team_skills = [team.team.skill for team in main.team_list]
    team_match_points = [team.match_points for team in main.team_list]
    team_skill_rank = [sorted(team_skills).index(team) for team in team_skills]
    team_match_point_rank = [sorted(team_match_points).index(team) for team in team_match_points]
    distortions = list(map(operator.sub, team_skill_rank, team_match_point_rank))
    return sum([abs(distortion)/number_of_teams for distortion in distortions])

def measure_distortions_over_adding_matches(start,stop,number_of_teams,iters):
    distortion_dicts = []

    for i in range(iters):
        if not i % min(100, 10**math.floor(math.log10(iters-1))):
            print(f"Iteration {i}")
        for number_of_matches in range(start, stop, 1):
            distortion_dicts.append(
                {"matches": number_of_matches, "distortions": get_distortions(number_of_teams, number_of_matches),
                 "even": 1 if number_of_matches % 2 == 0 else 0})

    df = pd.DataFrame.from_records(distortion_dicts)
    df.to_csv(datetime.now().strftime(r'results/distortions_matches_%m%d%Y%H%M%S.csv'), index=False)

def measure_distortions_over_adding_teams(start,stop,number_of_matches,iters):
    distortion_dicts = []

    for i in range(iters):
        for number_of_teams in range(start, stop, 1):
            distortion_dicts.append(
                {"matches": number_of_matches, "distortions": get_distortions(number_of_teams, number_of_matches)})

    df = pd.DataFrame.from_records(distortion_dicts)
    df.to_csv(datetime.now().strftime(r'results/distortions_teams_%m%d%Y%H%M%S.csv'), index=False)

def measure_combined_distortions(matches_start,teams_start,teams_stop,iters):
    distortion_dicts = []

    try:
        for i in range(iters):
            start_time = time.time()
            for number_of_teams in range(teams_start, teams_stop, 1):
                for number_of_matches in range(matches_start, math.ceil(number_of_teams/2)*2-2):
                    distortion_dicts.append({"matches": number_of_matches,
                        "teams": number_of_teams, "even": 1 if number_of_matches % 2 == 0 else 0,
                        "distortions": get_distortions(number_of_teams, number_of_matches)})
            end_time = time.time()
            print(f"Iteration {i} took time {end_time-start_time}")
    except KeyboardInterrupt:
        df = pd.DataFrame.from_records(distortion_dicts)
        df.to_csv(datetime.now().strftime(r'results/distortions_combined_%m%d%Y%H%M%S.csv'), index=False)
        raise KeyboardInterrupt

    df = pd.DataFrame.from_records(distortion_dicts)
    df.to_csv(datetime.now().strftime(r'results/distortions_combined_%m%d%Y%H%M%S.csv'), index=False)

def measure_distortions_over_two_numbers_of_matches(first,second,iters):
    distortion_dicts = []

    for i in range(iters):
        for number_of_matches in range(first, second+1, second-first):
            distortion_dicts.append(
                {"matches": number_of_matches, "distortions": get_distortions(16, number_of_matches)})

    df = pd.DataFrame.from_records(distortion_dicts)
    first_df = df[df['matches']==first]
    second_df = df[df['matches']==second]
    df.to_csv(datetime.now().strftime(r'results/distortions_6or7matches_%m%d%Y%H%M%S.csv'), index=False)
    tt = ttest_ind(first_df['distortions'], second_df['distortions'])
    print(tt)

def figure_out_what_is_wrong_with_the_means():
    # Used in the past to diagnose my issue with inconsistent test results
    # Diagnosis: Sample size issue
    scores = [[],[],[],[],[],[],[],[],[],[],[]]

    i = 0
    print("Iterations,1 match,2 matches,3 matches,4 matches,5 matches,6 matches,7 matches,8 matches,9 matches,10 matches")
    while i < 25000:
        i += 1

        for j in range(1, 12):
            scores[j-1].append(get_distortions(16, j))

        if i % 100 == 0:
            score_string = ""
            for score_list in scores:
                score_string += "," + format(statistics.mean(score_list), '.5f')

            print(i, score_string, sep="")

if __name__ == "__main__":
    measure_combined_distortions(4,10,32,500)