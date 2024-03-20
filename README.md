# Resetting

data-driven default item stack components

component overrides go in the `components` subfolder under the namespace and file name of the item to override

json format
- boolean `replace`
  - if true ignores the original components of the item
  - optional default false
- boolean `defaults`
  - if true adds the default item components of max stack size lore enchantments repair cost attribute modifiers and rarity
  - optional default true 
  - ignored if replace is false
- object `components`
  - keys are component type ids
  - values are json representation of component values