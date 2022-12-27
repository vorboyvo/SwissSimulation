import builtins
import time

from division import Division

if __name__ == "__main__":
    main = Division(name="Main", team_list=None, no_of_teams=16, skill_style=1, seed=1005)
    main.rr_run_matches()
    print(main)
