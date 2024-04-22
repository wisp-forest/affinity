```json
{
  "title": "Staff Bundling",
  "icon": "affinity:nimble_staff{bundled_staffs:[{id:\"affinity:kinesis_staff\", Count: 1b}, {id:\"affinity:cultivation_staff\", Count: 1b}, {id:\"affinity:collection_staff\", Count: 1b}]}",
  "category": "affinity:equipment",
  "required_advancements": [
    "affinity:big_girl_decisions"
  ]
}
```

After constructing a number of {concept}Staffs{}, incurring a light headache due to inventory management issues is not an
uncommon occurrence.


Most conveniently, however, {concept}all Staffs can be trivially bundled{} together (taking up only a single inventory
slot) by simply {concept}right-clicking{} them onto each other.


The resulting {item}Staff Bundle{}

;;;;;

```xml owo-ui
<stack-layout>
    <children>
        <texture texture="affinity:textures/gui/wispen_testament.png" texture-width="512" texture-height="256"
                 u="381" v="186" region-width="70" region-height="70">
            <blend>true</blend>
        </texture>

        <item>
            <stack>affinity:nimble_staff{bundled_staffs:[{id:"affinity:kinesis_staff", Count: 1b}, {id:"affinity:cultivation_staff", Count: 1b}, {id:"affinity:collection_staff", Count: 1b}]}</stack>
            <set-tooltip-from-stack>true</set-tooltip-from-stack>

            <sizing>
                <horizontal method="fixed">64</horizontal>
                <vertical method="fixed">64</vertical>
            </sizing>
        </item>
    </children>

    <vertical-alignment>center</vertical-alignment>
    <horizontal-alignment>center</horizontal-alignment>

    <sizing>
    <horizontal method="fill">100</horizontal>
    </sizing>
</stack-layout>
```

functions exactly as its central staff does for all intents and purposes. This central staff {concept}can be exchanged
at any time{}...


@next-page

...by pressing <keybind;key.affinity.select_staff_from_bundle> while holding the bundle.


Further, all contained staffs {concept}can be separated from the bundle{} one at a time by simply
{concept}right-clicking{} it in the inventory.

@entry-end