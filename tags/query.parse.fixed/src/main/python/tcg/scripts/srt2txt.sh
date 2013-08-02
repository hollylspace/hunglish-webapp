#!/bin/bash

# Minden csak szamot tartalmazo sort elhajit.
# Minden [badaf] {sdfgs} es <sddfafda> szovegdarabot agressziven elhajit.
# (Bar a [] neha tulnyulik a soron, olyankor nem.)
#
# Emphasis stripping. Legyalulja ezeket: ## @@ **

tr -d '\r' | grep -v "^$" | grep -v "^[0-9]*$" | grep -v "[0-9][0-9]:[0-9][0-9]:[0-9][0-9]" |\
sed "s/<[^>]*>//g" | sed "s/\[[^]]*\]//g" | tr '|' ' ' | sed "s/{[^}]*}//g" |\
sed "s/##*//g" | sed "s/@@*//g" | sed "s/\*\**//g"
