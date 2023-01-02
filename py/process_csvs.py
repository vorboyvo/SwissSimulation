import glob
import pandas as pd
import matplotlib.pyplot as plt
glob.glob("Work/*.csv")
ls = []
for file in glob.glob("Work/*.csv"):
    ls.append(pd.read_csv(file, header=0, index_col=None))
frame = pd.concat(ls, axis=0, ignore_index=True)
del ls
del file
dfs = {}
for i in range(10, 38):
    df2 = frame.groupby("teams").apply(lambda x: x if (x["teams"] == i).any() else None).reset_index(drop = True).groupby("matches", as_index=False)['distortions'].mean()
    dfs[i] = df2
del i
del df2
meanframe = pd.concat(list(dfs.values()), ignore_index=True)

# To save graphs over a dict of teams
def saveGraphs(dfs):
    for i in dfs:
        dfs[i].plot.scatter(x='matches',y='distortions')
        plt.title(f"{i} teams, means of distortions by # of matches")
        plt.savefig(rf'Work/graph_distortions_{i}teams.png')