# CTCTrees
Matching trees for Crack The Clue

## How does it work?

### Recognizing trees

Tree finding is just brute force - check every pixel in the world map for trees. This takes some time to do (20 minutes - 1 hour on a Macbook Air).

We find pixel-perfect matches in the world map using the sample tree images in the /trees directory. If a certain percentage of the pixels in a small window matches those of a sample tree, then we have found a tree in the world map. Unfortunately, when pixels are blocked, or when trees overlap one another, we both hallucinate trees and fail to detect some trees. Hopefully this is not too big of an issue.

Notably, we exclude yew trees, some desert trees(?), cacti, swamp plants, other plants, and those plant things(?) in Tirannwn.

The full list of trees found on the world map using this program is uploaded [here](http://pastebin.com/GKSamjU9). The three integers per line are x position, y position, and tree type (match the number with the image in the /trees directory).
 
### Matching groups of trees to the stacked clues



Does it work?

Seems like it:
![close match](http://i.imgur.com/ahxxWCr.png)

But the algorithm only checks trees if there is 

Match with 1 pixel error threshold (per tree):
![tree match with fuzz=2](http://i.imgur.com/6AXAxHI.png)

2 pixel error threshold:
![tree match with fuzz=2](http://i.imgur.com/KMH9LKC.png)

3 pixel error threshold:
![tree match with fuzz=2](http://i.imgur.com/OkKfNy5.png)

5 pixel error threshold:
![tree match with fuzz=2](http://i.imgur.com/ovxFEBj.png)
