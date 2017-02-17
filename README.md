# TestCaseAnalyzer

This is a simple JavaFX application written to assist my development team in the updating of our Selenium test suite.

It is designed to search the current repository directory set in the configuration for the test case name provided, then return a list of keywords and test data used by the test case.

Keywords can be rearranged via drag and drop, and can also be added and deleted. The Add Keyword button opens a dialogue window that allows the user to search for specific keywords to add. Keywords can also be directly edited by double-clicking the keyword to be edited and changing its contents.

Test data is also editable by double clicking and editing the content of the cell. Test data cells that contain data are highlighted in green.

In order to save any changes made, click the "Save Changes" button. Changes can be reverted by using the "Revert Changes" button. Once the "Save Changes" button is clicked, the name of the datasheet containing the data is added to the "Recently Updated Sheets" list in the center of the screen.

Note:
Sheets cannot be updated by the application if they are currently open. Please close the datasheet file before using the "Save Changes" button in the application.

SETUP INSTRUCTIONS:

To setup the application using the example data provided, follow the instructions below:
- Download and unzip the project
- from the project folder, go to the Example directory and double click the "TestCaseAnalyzer.jar" to run the application
- Once the application has started, click File and select Config, then select the directory "Example". For example, if you unzip the project to your desktop on a Windows machine, then your Config directory should look like "C:/Users/[your user]/Desktop/TestCaseAnalyzer/Example".
- Once the config is set, you can use the following fake test case names to see the application in action: "Example Test Case 1", "Example Test Case 2"
