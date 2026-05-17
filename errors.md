Bug Report for AI Agent
Issue Summary

The mobile UI on the Player screen is not rendering correctly on smaller screens/devices. The layout appears broken and some functionality crashes the app.

1. Player Screen Rendering Issue
Observed Behavior
The Player, Queue, and Library tabs are positioned too high at the top of the screen.
Tabs are not respecting safe area / screen padding.
Most of the screen is completely blank.
Only a small horizontal dash/loading indicator appears in the center.
UI looks improperly scaled or not responsive for the device.
Expected Behavior
Tabs should be properly aligned within the visible safe area.
Player content should render correctly and occupy the screen layout.
Responsive layout should adapt to mobile screen dimensions.
Possible Cause
Missing safe-area handling (SafeAreaView, paddingTop, status bar handling, etc.)
Incorrect flex layout or height calculations.
Responsive styles not adapting to smaller screens.
Player content container may not be rendering or is collapsing.
2. Library Tab Crash
Observed Behavior
Tapping the Library tab immediately crashes the app.
Expected Behavior
Library screen should open normally without crashing.
Possible Cause
Navigation route issue.
Undefined/null data access.
Component import/export issue.
State initialization failure.
Runtime exception inside Library screen component.
Device Context
Issue observed on Android device.
Screenshot shows severe mobile responsiveness/layout problems.
