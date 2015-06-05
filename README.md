
# How to run

Please make sure you have JDK 8 installed on your machine as that was the version used to develop the code.  Best viewed through an IDE such as Intellij.  This will allow you to explore the code easily and run the tests.   If you would like to have an executable or some other means to display the levels please let me know.


# Levels

Below will describe how I implemented the solution to each level along with which Class::method's  are relavent to that level.  

Also , tests displaying completeness of each level are located in src/test/com/blakec/LevelTest.java

## Level 1 - Validate and print path

### Implementation:

A Postion represents a (row, column) location on a board.
A Board knows its width and height as well as maintains sets of special positions.  I.e. positions such as water, lava.. etc
A path can represented as a List of Positions.  

#### Validation
This path can be validated by check if all subsequent positions in the path have a distance that is equal to a knights movement, no barrier exists between them and the positions do not reside on a barrier or rock.

#### Board Printing
A board can be printed by iterator over all possible positions on the board and printing the '.' character and '\n' characters at the end of each row.  Should a position be in the boards set of special positions, that special position will be printed.  If the position contains the knight, a 'K' position will be printed.

#### Code Location ####

- Board::isValidSetOfMoves
- Board::isValidMove
- Board::isValidPosition
- Board::doesMoveHitBarrier
- Board::printKnightsPathOnBoard
- Board::printBoardWithKnightAtPosition
- Board::getPositionType

## Level 2/3 - Generate valid (and shortest) path between two positions 8x8 board

### Implementation

If we solve level 3, we also solve level 2.  The board can be thought of as a graph.  Each node in the graph is a position on the board.  An edge exists between Vertex A and Vertex B, if a knight can travel from Position A to Position B in one move.  The weight of each edge is 1.  Given Position A and Position B, a shortest path from A to B can be generated by running Dijkstra's Algorithm on the graph constructed from the board.  In my implementation, I use this Algorithm with a min-Priority queue to run in O(|E|+ |V|log|V|) where |V| is the number of nodes in the graph and |E| is the number of edges.

An example of what the graph would look like for a 4x4 grid is below:

![4x4 simple grid graph](https://github.com/pequnio3/board/blob/master/simple_board.jpg)

#### Code Location ####

- Board::computeShortestPath
- Board::generatePositionGraph
- Graph::computeShortestPath
- FibonacciHeap # Note this was not implemented by me and instead was an efficient and reliable implemntation I found online

## Level 4 - Generate shortest path between two positions on 32x32 board with special positions.

### Implementation

Implementing Level 4 involved building on top of level 3 by adding different weights on edges.  
One can think of Moving from Position A to Position L (lava) as traversing an edge that has a weight 5 instead of 1.  
One can think of Moving from Position A to Position W (water) as traversing an edge that has a weight 2 instead of 1.
A teleporter at Position T can be represented as a Vertex T that is connected by an edge of weight 0 to all other teleporter vertices in addition to the other vertex positions that a knight could jump to.

In addition a Position A may no longer be conected to other positions that a knight could normally jump to if the end position is a rock/barrier or if the barrier is along the L-shaped path the knight would move.

These factors are accounted for when I generate the board graph.

#### Code Location 

- Board::generatePositionGraph
- Board::isValidSetOfMoves
- Board::isValidMove
- Board::isValidPosition
- Board::doesMoveHitBarrier

## Level 5 - Generate longest path between two positions on 32x32 board with NO special positions.

### Implementation

Whew! this one was a doozy.  It has elements very similar to that of the Knights Tour problem.  A Knights tour is a series of movements a knight can make on a board such that he visits all squares without repeat.   A way to find a Knights tour is by performing a depth first search that aims to reach a depth equal to the number of positions on the board.  

#### Warnsdorf's Rule
On an 8x8 board DFS can end up taking a long time.  An improvement can be made by using Warnsdorf's rule to order the next visted neighbors by having least number of neighbors.  I.e.  choose the next node to visit by finding the neighbor closest to the outside of the board.  Having fewer neiighbors prevents the algorithm from getting stuck down a search path that is too big and may not be successful.  


A Knights Tour, however, does not specify an end position.  Only a start position.  I however modified the DFS w/ Warnsdorf's rule to be searching for paths to the target position.  

#### Max Path Length

A chess board has colored squares (black and white) alternating.  When a knight move's it must move from its positions color, to a position with an opposite color.  In finding the longest path length, we can use this as an upper bound for path size.  

-	If the starting and ending positions have opposite colors this means the number of vertices in the longest path is equal to the number of positions on the board.  
-	If the starting and ending positions have the same color this means the number of vertices in the longest path is equal to the number of positions on the board minus one.  

Using this thought, I further modified my DFS w/Warnsdorf to specify a max path length so that once a path to the target node is reached with max length, the algorithm exits.

Note:  Even though these heuristics improve the running time in practice, it is still possible for the algrithm to get stuck in a deep search and take a long time.


#### 32x32 board

On an 8x8 board my DFS works pretty quickly with most start and end positions.  However when extended to 32x32 Warnsdorf's rule is less effective and results in my DFS running forever.  To improve upon this, I divided the 32x32 board into 16 8x8 sub-boards.  The start position will now be in one of the sub-boards and the end position will be in one of the sub-boards.  We can now create a path from the start board to the end board that visists all the other boards.  This path is generated by running the DFS longest path algorithm on a graph formed from this 4x4 sub-board grid.  Now, we can have our longest path emulate this sub-board path by following the sub board path and generating a knights tour in each.

The knights tour in each board is generated by specifying a start position and end position that links adjacent sub boards, so that it takes a single move to move from one sub board to another.  In the code I reference these as 'Linking Positions'.  I.e. positions on a prev and next sub board that will link them together.

In addition I make sure that the end position in a sub board is the opposite color of the entrace position as to ensure a full Knights tour within the 8x8 board.


#### Same start/end sub-board special case

This algorithm breaks down a little when the start and end position are in the same sub-board.  The currently described algorithm would run a full nights tour in the start-board and by the time we get to end board (which is the start board) all those positions will be used up.

To solve this we can change the start position to an adjacent board.  We do this by manually determining which adjacent sub board (north,south,east, west) is closest to the start position and then manually making the fewest number of moves to move the knight out of the start board into the adjacent board.  We add these moves as the first moves in our longest path.  The first position in the adjacent board is now our new start position.  We run the algorithm as normal from here until we get to the end board.  Once the algorithm is about to run the DFS on the end board, we modify the endboards graph with the first few positions removed.  

**NOTE: I did not have time to implement this special case nor am I quite sure whether it results in an optimal longest path, however I figured this would be an appropriate way to inch my solution closer to optimal.**

#### Future Work

THe DFS cant take too long in certain circumstances.  The paper https://larc.unt.edu/ian/pubs/algoknight.pdf mentions a similar divide and conquor strategy though it appears as though when touring through blocks, they have single preplanned tours that make joining the blocks simple.  To make my algorithm faster, I have a set of precomputed paths for transitioning from one sub-board to another.  This way the majority of the sub-boards are already computed and computation only needs to take place on the start and end boards.


#### Code Location

- Board::computeLongestPath
- Board::computeLongestPathBruteForce
- Board::computeLinkPosition
- Board::generateSubBoardGraph
- Board::generateNeighborSubBoards
- Graph::computeLongestPath
- Graph:computeLongestPathRecursive
- Graph::getNeighborsSortedByFewestNeighbors