# CTCTrees
Matching trees for Crack The Clue

One provides a tree pattern image as input, and the program spits out a world map with the top matched tree groups circled, ranging from green (relative high match) to blue (relative low match). A similar version of the code is running live on the /r/CrackTheClue subreddit, where a bot responds to `!TreeSearch [imgur url]` commands.

Here is an example run of no particular merit:
![An example run](http://i.imgur.com/yiJSmpH.png)

## How does it work?

There are 3 steps, and 3 important parameters to tune.

### 1. Recognizing trees on the world map

Tree finding is just brute force - check every pixel in the world map for trees. This takes some time to do (20 - 30 minutes on a Macbook Air). This only needs to be done once, though. Trees that are found will be saved to the savefile `data/trees.txt`. This repo comes with a savefile, so you can skip most of the processing.

We find pixel-perfect matches in the world map using the sample tree images in the /trees directory. If a certain percentage (80%, don't ask why) of the pixels in a small window matches those of a sample tree, then we have found a tree in the world map. Unfortunately, when pixels are blocked, or when trees overlap one another, we both hallucinate trees and fail to detect some trees. Hopefully this is not too big of an issue for now. However, I believe that missing even a few trees in our tree list is highly detrimental to the success rate of our tree matcher, so making the tree recognizer better would be a good contribution for someone new who wants to help out.

Notably, we exclude rare tree icons, some desert trees(?), cacti, swamp plants, other plants, and those plant(?) things in Tirannwn.

The full list of trees found on the world map using this program is uploaded [here](http://pastebin.com/GKSamjU9). The three integers per line are x position, y position, and tree type (match the number with the image in the /trees directory, or look in the source code).

### 2. Matching groups of trees to the stacked clues

The algorithm for matching trees has changed vastly since the first iteration of the program and has been greatly improved.

The layout of trees from stacked clues (from [/u/Ninjamark1991's post](https://www.reddit.com/r/CrackTheClue/comments/4vynzy/using_trees_as_a_clue/) on Reddit) is matched to trees in the world map. We sample the locations of those trees by marking each tree with a single red (255,0,0) pixel at the bottom (data/clue1.png) or at the top (data/clue2.png). This lets the program know where each tree is. We refer to this image as a "tree pattern" image.

Matching this tree pattern is done by picking the closest pair of trees in the tree pattern as our "key" or "anchor" trees, A and B. We then rotate the tree pattern, as well as the entire world map, by an angle theta such that A and B are vertically aligned after rotating. Also, we enforce that A is "above" B, or has a lower y-value in the left-handed coordinate system. Now examining the trees in the world map, iterating through each A candidate, we pick another B candidate that is directly below the A candidate after rotating. Using the positions of the A and B candidates, we can determine where the other trees in the pattern ought be on the world map (if the pattern is accurate), and we find the tree closest to the expected position using a grid-based method using tiles. A and B must be within 1 tile size of each other on the world map for the pair to be considered. To account for inaccuracies in the pattern, we use an error tolerance per tree. There is ample room for improvement in dealing with pattern inaccuracies, as large test cases don't seem to work very well.

### 3. Filtering out false positives

This is a very important step, and logically it occurs simultaneously with step 2. We assume that the tree pattern is drawn such that there are no other trees in the vicinity (otherwise, they would have been included in the pattern). This lets us weed out ~95% of the false positives, making the output map much much cleaner. Specifically, we check if there exist trees between our matched trees. If there is even one tree in between pairs in our matched trees, then we treat the match as a false positive. Other filtering steps include verifying a low error in the relative span of the match and that of the pattern, and sorting the matches by normalized squared error.

### Parameters

*Top K matches* - the default is *50* - Pretty self-explanatory: the maximum number of (top) results to show.

*Pixel tolerance* - the default is *8* - The maximum distance in pixels between expected tree positions and actual tree positions, per tree. Increasing this value gives more and more incomprehensible results. Decreasing this value seeks only close to perfect matches.

*Tile size* - the default has little meaning to non-contributors - The lower this is, the faster the grid-based search runs. However, with high pixel tolerance, you need a large tile size.
