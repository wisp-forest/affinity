```json
{
  "title": "Automated Assembly",
  "icon": "affinity:carbon_copy{Recipe: \"affinity:crafting/assembly_augment\", Result: {id: \"affinity:assembly_augment\", Count: 1b}}",
  "category": "affinity:basics",
  "associated_items": [
    "affinity:carbon_copy"
  ],
  "required_advancements": [
    "affinity:i_still_use_paint"
  ]
}
```

One of [Anthracite Dust](^affinity:anthracite_extraction)'s more mundane properties is that, when {concept}used on an
[Assembly Augment](^affinity:assembly_augment) with a valid recipe{} in it, a {item}Carbon Copy{} of said recipe is
created. This is, in effect, a neat summary of how the recipe is crafted - comprehensible by both you and your machinery.


Crucially, the {item}Assembly Augment{} is capable of acting on the instructions encoded

;;;;;

by a {item}Carbon Copy{}. Once inserted into the {concept}slot to the left of the crafting grid{}, the {item}Assembly
Augment{} will {concept}craft the specified recipe{} - granted that sufficient materials are supplied.


It is worth noting that contrary to more primitive instruments (like the Crafter), the {item}Assembly Augment{}
intelligently distributes items it receives via hoppers or, preferably, [Item Transfer Nodes](^affinity:item_transfer_node)
among its relevant input slots.