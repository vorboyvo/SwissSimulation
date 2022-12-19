from team import Team
from tfmatch import Match

home = Team("Good team",0.25)
away = Team("Open shitters",-0.25)

home_rounds = 0
away_rounds = 0

for i in range(500):
    match = Match(home_skill=home.skill,away_skill=away.skill,koth=True)
    home_rounds += match.home_rounds_won
    away_rounds += match.away_rounds_won

print(home_rounds/(home_rounds+away_rounds))