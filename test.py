from division import Division

my_list = ["a", "b", "c", "d", "e", "f", "g", "h"]


for i in range(100):
    invite = Division(name="Invite", team_list=None, no_of_teams=8, skill_style=2)
    invite.rr_run_matches()
    print("Invite " + str(i) + " \n" +
    str(invite))