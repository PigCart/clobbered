# Clobbered
Throw items with force! Clobber mobs and players by lobbing things at them.

## Lobbed Items
- ✔ Items can be dropped like normal by pressing the drop key.
- ✔ Holding the drop key will throw the item farther. The longer you hold the drop key the more forcefully the item will be thrown.
- ✔ Items thrown forcefully enough will do minor damage based on their velocity.
- ✔ Items that gain speed after being dropped (e.g: by falling from a great height) will deal damage as if they were lobbed
- ✔ Items for blocks made of tough materials will do more damage (e.g: obsidian)
- ✔ The initial velocity of thrown items is affected by the player's velocity at the time of throwing
- ✔ Lobbed items can be caught by interacting with them with an empty hand
  - ✔ Items tagged `#clobbered:uncatchable` can not be caught
- ✔ Items tagged `#clobbered:soft` will never deal damage when thrown
- ✔ Items tagged `#clobbered:skippable` have a chance to bounce when thrown on water at a shallow angle
- ✔ Items tagged `#clobbered:blacklisted` will be dropped as a normal item instead of lobbed or hurled
- ✔ Items will get stuck to entities if either the entity or item is tagged `#clobbered:sticky`
- ✔ Thrown items that hit or impale a target block will trigger it

## Hurled Items
- ✔ Items thrown with maximum force will be hurled.
- ✔ Hurled item will fly straight and with more accuracy
- ✔ Hurled tools and weapons will strike a hit entity as if the player had attacked normally, applying enchantment effects, attributing kills, and losing durability.
- ✔ Items tagged `#clobbered:sharp`, will impale blocks or entities they hit
  - ✔ Entities will not despawn while an item is stuck in them
  - ✔ When the entity dies the item is dropped
  - ✔ Right-clicking on a stuck item will pick it up
  - ✔ Like arrows, hurled items that impale a button will trigger the button
- ✔ Items tagged `#clobbered:consumed` will use their default interaction instead of being thrown as an item (e.g: snowballs)
- ✔ Items tagged `#clobbered:explodes` will explode upon hitting a block or entity when hurled
- ✔ Items tagged `#clobbered:boomerang` will return to the position they're hurled from instead of falling to the floor
- ✔ Blocks can become cracked or broken on impact (eg: stone → cobblestone, anvil → damaged anvil)

## Misc
- ✔ Shot arrows can be picked up the same way as stuck items.
- ✔ Boomerang item. Uses the boomerang tag mechanic.
  - Can take trident enchants (e.g: loyalty)
- ✔ 'Kick' button that imparts a force on nearby entities
- ✔ Stuck item retrieval config
  - ✔ automatic (like vanilla)
  - ✔ interaction - must retrieve by interacting with the item (default right click)
  - ✔ mob death - retrieve with interaction except mobs, who have to be killed to drop the item
- ✔ Whitelist & Blacklist for players who are allowed to throw