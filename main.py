import operator
import random
import statistics
from datetime import datetime

import numpy as np
import pandas as pd
from scipy.stats import ttest_ind, t

from division import Division

def get_distortions(number_of_matches):
    main = Division(name="Main", team_list=None, no_of_teams=16, skill_style=1)
    random.shuffle(main.team_list)
    try:
        main.swiss_run_matches(number_of_matches)
    except Exception:  # Cope and seethe, PyCharm Professional
        print("Fucked up!")
        return None
    team_skills = [team.team.skill for team in main.team_list]
    team_match_points = [team.match_points for team in main.team_list]
    team_skill_rank = [sorted(team_skills).index(team) for team in team_skills]
    team_match_point_rank = [sorted(team_match_points).index(team) for team in team_match_points]
    return list(map(operator.sub, team_skill_rank, team_match_point_rank))

def measure_distortions_over_adding_matches():
    distortion_dicts = []
    errors = 0

    for i in range(50):
        print(f"Running iteration {i}")
        for number_of_matches in range(5, 16, 1):
            print(f"Running Swiss division with {number_of_matches} matches")
            distortions = get_distortions(number_of_matches)
            distortion_dicts.append(
                {"matches": number_of_matches, "distortions": sum([abs(distortion) for distortion in distortions])})

    df = pd.DataFrame.from_records(distortion_dicts)
    df.to_csv(datetime.now().strftime(r'results/distortions_matches_%m%d%Y%H%M%S.csv'), index=False)


def measure_distortions_over_two_numbers_of_matches(first,second,iters):
    distortion_dicts = []
    errors = 0

    for i in range(iters):
        #print(f"Running iteration {i}")
        for number_of_matches in range(first, second+1, second-first):
            #print(f"Running Swiss division with {number_of_matches} matches")
            main = Division(name="Main", team_list=None, no_of_teams=16, skill_style=1)
            random.shuffle(main.team_list)
            try:
                main.swiss_run_matches(number_of_matches)
            except Exception:  #Cope and seethe, PyCharm Professional
                errors += 1
                print("Fucked up!")
                continue
            team_names = [team.team.name for team in main.team_list]
            team_skills = [team.team.skill for team in main.team_list]
            team_match_points = [team.match_points for team in main.team_list]
            team_skill_rank = [sorted(team_skills).index(team) for team in team_skills]
            team_match_point_rank = [sorted(team_match_points).index(team) for team in team_match_points]
            distortions = list(map(operator.sub, team_skill_rank, team_match_point_rank))
            distortion_dicts.append(
                {"matches": number_of_matches, "distortions": sum([abs(distortion) for distortion in distortions])})

    df = pd.DataFrame.from_records(distortion_dicts)
    first_df = df[df['matches']==first]
    second_df = df[df['matches']==second]
    #df.to_csv(datetime.now().strftime(r'results/distortions_6or7matches_%m%d%Y%H%M%S.csv'), index=False)
    tt = ttest_ind(first_df['distortions'], second_df['distortions'])
    print(tt)

def figure_out_what_is_wrong_with_the_means():
    scores = []

    i = 0
    print("stuff,overall avg,last 100 matches avg")
    while i <= 25003:
        i += 1

        distortions = get_distortions(6)
        scores.append(sum([abs(distortion) for distortion in distortions]))


        if i % 100 == 0:
            print(i, format(statistics.mean(scores), '.5f'), format(statistics.mean(scores[-100:]), '.5f'), sep=",")

figure_out_what_is_wrong_with_the_means()