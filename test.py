import builtins
import time

from division import Division
from divisionP import blossom_swiss_run_matches

"""
from division import Division

debug_file = open('test.txt', 'w')

def debug_print(*args, **kwargs):
    builtins.print(*args, file=debug_file, **kwargs)

for i in range(20):
    print(f"Running Swiss division with 14 matches {i}")
    main = Division(name="Main", team_list=None, no_of_teams=13, skill_style=2, debug_print=debug_print)
    # debug_file.seek(0)
    # debug_file.truncate(0)
    # time.sleep(2)
    try:
        main.swiss_run_matches(11)
    except Exception as err:  #Cope and seethe, PyCharm Professional
        # time.sleep(3)
        # debug_file.close()
        raise err
"""

main = Division(name="Main", team_list=None, no_of_teams=16, skill_style=1, seed=10)
main.swiss_run_matches(5)
print(main)