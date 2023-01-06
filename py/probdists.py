import glob
import math

import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns

# Constants
playoffs_proportion = 0.5

for divsize in range(8, 37):
    playoffteams = math.ceil(divsize * playoffs_proportion / 2) * 2  # We always want even # of playoffs teams

    # Read file
    # Assumption is there is only one of those files in this dir
    print(divsize)
    file = glob.glob(f'Work/temp/standings_weeks_{divsize}teams*.csv')[0]
    frame = pd.read_csv(file)
    frame['week'] += 1
    frame['skillRank'] += 1
    frame['leagueTableRank'] += 1

    # Consolidate into probability distribution chart
    df = pd.DataFrame(index=np.arange(1, divsize + 1, 1))
    for week in range(1, math.ceil(divsize / 2) * 2 - 2):
        df[week] = pd.crosstab(frame[frame['week'] == week].skillRank, frame[frame['week'] == week].leagueTableRank,
                               normalize='columns').drop(columns=range(playoffteams + 1, divsize + 1, 1)).sum(axis=1)

    # Plot it
    cmap = sns.color_palette("flare", as_cmap=True)
    fig, ax = plt.subplots(figsize=(15, 15))
    sns.heatmap(df.round(2), annot=True, annot_kws={"size": 35 / np.sqrt(len(df))}, cmap=cmap)
    plt.xlabel("Week in season")
    plt.title(f"Prob for n-th team out of {divsize} ranked by skill to be in {playoffteams}-team playoffs")
    plt.savefig(f"Work/temp/{divsize}playoffs{playoffteams}probplot.png")
    plt.close()
