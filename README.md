# CTCTrees
Matching trees for Crack The Clue

## How does it work?

### Recognizing trees

Tree finding is just brute force - check every pixel in the world map for trees. This takes some time to do (20 minutes - 1 hour on a Macbook Air).

We find pixel-perfect matches in the world map using the sample tree images in the /trees directory. If a certain percentage of the pixels in a small window matches those of a sample tree, then we have found a tree in the world map. Unfortunately, when pixels are blocked, or when trees overlap one another, we both hallucinate trees and fail to detect some trees. Hopefully this is not too big of an issue.

Notably, we exclude yew trees, some desert trees(?), cacti, swamp plants, other plants, and those plant things(?) in Tirannwn.

The full list of trees found on the world map using this program is uploaded [here](http://pastebin.com/GKSamjU9). The three integers per line are x position, y position, and tree type (match the number with the image in the /trees directory).

### Matching groups of trees to the stacked clues

This part of the project is experimental. The layout of trees from stacked clues (from [/u/Ninjamark1991's post](https://www.reddit.com/r/CrackTheClue/comments/4vynzy/using_trees_as_a_clue/) on Reddit) is matched to trees in the world map.

This is done efficiently by noticing that [these trees](http://i.imgur.com/JBTG048.png) are vertically aligned. We can sort the list of trees that we have found by x position, then y position. Now, the tree following tree T in the sorted list will be the one that is immediately below tree T. Assuming we interpret the circled trees in the image as those two trees, we can calculate where the other 17 trees ought to be on the world map, and check whether or not they are there by fuzzy binary search. Error tolerances of 1 - 5 pixels per tree are used (see results below).

## Does it work?

Seems like it does:
![close match](http://i.imgur.com/ahxxWCr.png)

This is a close match (made with an error tolerance of 1 pixel), and it's the only match found on the entire world map, but it seems unlikely to be a lead. Other results are just as unlikely to be leads. Most of the matches are clumps of trees, or hallucinated trees. Most importantly, the algorithm might miss certain clusters due to intrinsic limitations of the algorithm, as a result of constraining two of the trees to be vertically aligned.

This is where you come in. Go wild with the [tree data](http://pastebin.com/GKSamjU9)!

## Some results

Match with 1 pixel error tolerance (per tree):
![tree match with fuzz=2](http://i.imgur.com/6AXAxHI.png)

2 pixel error tolerance:
![tree match with fuzz=2](http://i.imgur.com/KMH9LKC.png)

3 pixel error tolerance:
![tree match with fuzz=2](http://i.imgur.com/OkKfNy5.png)

5 pixel error tolerance (for fun):
![tree match with fuzz=2](http://i.imgur.com/ovxFEBj.png)
