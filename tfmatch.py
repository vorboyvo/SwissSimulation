from random import randrange

import numpy


class Match:
    def __init__(self, home_skill: float, away_skill: float, koth: bool):
        """
            koth is a boolean value, taking on false if the match is stopwatch and true if the match is koth

            The difference in skill (home - away) is a proxy for how much more likely one team is to win ANY GIVEN ROUND than the other.
            It is passed as a parameter to a logistic equation f(x) = 1 / (1 + e^-(jx))

            Choosing exogenous j such that it is the lowest j that gives an average size of distortions relative to skill
                less than 500, so that each match, on average, has less than 5 distortions.

            We run the match until we have a winner, i.e. at least one team has won 4 rounds on koth or 2 on stopwatch.
        """
        n = home_skill - away_skill
        home_win_chance = 1 / (1 + numpy.exp(-2 * n))

        # Run first to 4 on koth or 2 on stopwatch
        if koth:
            winlimit = 4
        else:
            winlimit = 2

        home_rounds_won = 0
        away_rounds_won = 0
        while home_rounds_won < winlimit and away_rounds_won < winlimit:
            round_outcome = run_round(home_win_chance)
            if round_outcome:
                home_rounds_won += 1
            else:
                away_rounds_won += 1

        # Treat stopwatch rounds as 2 each
        if not koth:
            home_rounds_won *= 2
            away_rounds_won *= 2

        self.home_rounds_won = home_rounds_won
        self.away_rounds_won = away_rounds_won
        self.winner = True if home_rounds_won > away_rounds_won else False #Winner is TRUE if home wins, FALSE otherwise
        self.home_match_points, self.away_match_points = get_match_points(self.home_rounds_won, self.away_rounds_won)
        self.home_inq_match_points, self.away_inq_match_points = get_inq_match_points(self.home_rounds_won, self.away_rounds_won)

        self.home_match_result = MatchResult(self.winner, self.home_rounds_won, self.away_rounds_won, self.home_match_points, self.home_inq_match_points)
        self.away_match_result = MatchResult(not self.winner, self.away_rounds_won, self.home_rounds_won, self.away_match_points, self.away_inq_match_points)

    def get_home_result(self):
        return self.home_match_result

    def get_away_result(self):
        return self.away_match_result

    def __repr__(self):
        return str(self.home_rounds_won) + "-" + str(self.away_rounds_won)

class MatchResult:
    def __init__(self, won: bool, rounds_won: int, rounds_lost: int, match_points: int, inq_match_points: float):
        self.won = won
        self.rounds_won = rounds_won
        self.rounds_lost = rounds_lost
        self.match_points = match_points
        self.inq_match_points = inq_match_points

def run_round(home_win_chance):
    return randrange(100) < home_win_chance * 100

def get_match_points(home_rounds_won, away_rounds_won):
    # Returns match points as a tuple home, away
    koth = False
    if max(home_rounds_won,away_rounds_won) == 4:
        koth = True

    if not koth:
        home_match_points = round((home_rounds_won / (home_rounds_won + away_rounds_won)) * 9)
        away_match_points = 9 - home_match_points
    else:
        home_match_points = 0
        away_match_points = 0
        if home_rounds_won > away_rounds_won:
            home_match_points += 6
            away_match_points = away_rounds_won
            home_match_points += 3 - away_match_points
        else:
            away_match_points += 6
            home_match_points = home_rounds_won
            away_match_points += 3 - home_match_points

    return home_match_points, away_match_points

def get_inq_match_points(home_rounds_won, away_rounds_won):
    return home_rounds_won * 9 / (home_rounds_won + away_rounds_won), away_rounds_won * 9 / (home_rounds_won + away_rounds_won)