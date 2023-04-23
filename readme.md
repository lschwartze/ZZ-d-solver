ZZ-d solver for standard 3x3 Rubik's Cube

# base puzzle
The 3x3 is modelled in cube.java, corner.java and edge.java. Each piece is assigned a name that references the colours on it. Each piece furthermore has attributes for its current position and its solved positions as Strings. Finally an attribute for orientation as integer.

# the solver
After implementing logic that applies turns, the solver becomes a mere formality. For each step an initial solution is created using basic depth search. This is optimised by making sure that the same side isn't turned twice consecutively (like R R2). Also, opposite sides aren't turned three times (like R L R).
Afterwards this initial solution is optimised and each step is solved in the least number of moves required. 
Note that the depth search is further optimised by removing faces from the available moves. That's a consequence of the workings of ZZ-d and can't be generalised to (most) other methods. This ensures a faster computation.
Last Layer algorithms are optimal (via cube explorer)