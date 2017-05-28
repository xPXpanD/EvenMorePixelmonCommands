# PixelUpgrade
A Minecraft plugin, meant to work with Pixelmon. Change just about everything Pokémon-related, using economy integration!

Stock configs are external to the project, located in PROJECT FOLDER/build/classes/main/assets/pixelupgrade.
Link for these coming soon, but have an example:

# ------------------------------------------------------------------------ #
# PIXELUPGRADE CONFIG FILE FOR /CHECKEGG
# DEFAULTS: 0, false, true, 25
# NEW VALUES SHOULD BE THE SAME FORMAT AS THE DEFAULTS
# EXAMPLE: true/false | 1 | 1.0 | "message"
# ------------------------------------------------------------------------ #

# Changes the verbosity of this command's debug logger. Cumulative.
# 0 = print critical errors only, 1 = +changed stats/balances, important exits
# 2 = +normal command start/exit, 3 = +ultra verbose bug tracking spam mode GO
debugVerbosityMode = 0

# Should we only give vague hints as to what's in an egg, or should we be explicit?
# Vague hints are things like "This baby seems to have an odd sheen to it...", etcetera.
# Explicit mode outright shows IVs plus shiny status. Both modes reveal the name.
explicitReveal = false

# If you want the command to charge money for a successful egg check, change this.
commandCost = 25

# Should checking the same egg again be free? This should persist across trades/GTS.
recheckIsFree = true
