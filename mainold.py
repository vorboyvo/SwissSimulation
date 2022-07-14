"""
In this code I liberally use tuples for my own ease. Creating an object is more correct, but this works equivalently
as long as I don't mess this up. Tuples are used in the following ways:
- To denote divs: (div name: string, div size: int, div style: string {"RR", "Swiss"})
- To denote a match outcome: (home team rounds: int, away team rounds: int)
- To denote match points: (home match points: float, away match points: float)
- To denote a team: (name: string, skill: int)
- To denote a team in the context of a div: (team: team, wins: int, losses: int, rounds won: int, rounds lost: int,
        match points: int)
"""

def generate_uniform_teams(division):
    print("Generating " + str(division[1]) + " teams for " + division[2] + " " + division[0])
    team_list = []
    for i in range(0,division[1]):
        team_list.append((("Team " + str(i+1), 0),0,0,0,0,0))
    return team_list

def generate_rr_schedule(division, teams_list):
    weeks = []
    #for i in range (1, 8)


# Define all divisions as tuples of their name, the number of teams inside, and whether they are RR or Swiss.
invite = ("Invite", 8, "RR")
advanced = ("Advanced", 12, "Swiss")
main = ("Main", 16, "Swiss")
intermediate = ("Intermediate", 24, "Swiss")
amateur = ("Amateur", 32, "Swiss")

div_list = [invite, advanced, main, intermediate, amateur]

for div in div_list:
    print("Running tests on division " + div[0] + " with " + str(div[1]) + " teams and using " + div[2])
    print("Test 1: All teams have same skill level")
    team_list = generate_uniform_teams(div)
