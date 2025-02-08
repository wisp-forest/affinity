```json
{
  "title": "Item Transfer Node",
  "icon": "affinity:item_transfer_node",
  "category": "affinity:magical_artifacts",
  "required_advancements": [
    "affinity:i_still_use_paint"
  ],
  "associated_items": [
    "affinity:item_transfer_node"
  ]
}
```

Suspending a number of items in midair using the [Wise Wisps'](^affinity:wisps) matter works out to be an excellent
methodology of {c}item transport{}. The {i}Item Transfer Nodes{} implement it, capable of sending and receiving items
up to 15 blocks away (this range may be visualized using the [Staff of Inquiry](^affinity:inquiry)).


To set up simple transport between containers, {c}place{}

;;;;;

<recipe;affinity:aspen_infusion/item_transfer_node>

{c}one node on each container{} (observing the container's respective sided transfer characteristics) and {c}link them{}
using...


@next-page

...a [Wand of Iridescence](^affinity:wand_of_iridescence).


Now, each node can be configured to {c}send items{} by {c}sneak-interacting{}. Here, it must be noted that {c}sending{}
nodes only send to {c}idle{} nodes, but not other {c}sending{} ones. 


The {c}amount of items{} to send at a time may be changed by {c}scrolling while sneaking and targeting the node{}.


Further, each node may be configured to only accept (or

;;;;;

refuse) one kind of item. To do this, {c}interact{} with the node to open its interface, inside which the filter can be
set by {c}clicking on the node preview{} using the desired filter item.


For when more general filtering is required, a node may also be configured to accept {c}all items in an item tag{}. To
specify a tag for filtering, {c}rename the filter{}


@next-page

{c}item{} in an anvil to state the tag ID with a {c}#{} prepended - like so:

<|item-spotlight@lavender:book_components|item=minecraft:oak_planks{display:{Name:'{"text":"#minecraft:planks"}'}}|>