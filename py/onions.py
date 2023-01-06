import glob
import math

import pandas as pd
import matplotlib.pyplot as plt

for i in range(8, 37):
    df = pd.read_csv(glob.glob(f"results/standings_weeks_{i}teams*.csv")[0])
    df += 1
    dfs = {}
    for j in range(1, i+1):
        dfs[j] = df.groupby("skillRank").apply(lambda x: x if (x["skillRank"] == j).any() else None).reset_index(
            drop=True).groupby("week", as_index=False)['leagueTableRank'].mean()
    ax1 = dfs[1].plot.line(x='week', y='leagueTableRank', label=f"{0} rank")
    for j in range(2, i+1):
        dfs[j].plot.line(x='week', y='leagueTableRank', label=f"{i} rank", ax=ax1)
    plt.title(f"The grand onion with {i} teams")
    plt.xlabel("Week of season")
    plt.ylabel("Place of team in given skill rank on the league table")
    plt.gca().invert_yaxis()
    plt.legend(bbox_to_anchor=(1.05, 1), loc='upper left', borderaxespad=0., mode='expand')
    plt.savefig(f"results/{i}teams.png")
    plt.close()
