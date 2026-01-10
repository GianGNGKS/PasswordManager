# PasswordManager
DIY Password manager I decided to make on my own as practice.

# TODO / Ideas
* UX
    - [ ] Clipboard integration
    > Add a command 'copy "servicename"' that copies a service's password straight to the user's clipboard. 
    - [ ] Password geneation
    > Allow the user to choose whether they want to add their own password or let the system create a new, safe, password on its own.
* GUI
    - [ ] JavaFX/Swing implementation
    > Console interaction is only temporary, add a snappy and simple UI for interaction, though still allow for the user to use commands if it's desired.
    - [ ] a
* Code quality/standards
    - [ ] Unit testing.
    > Learn to write proper unit testing, for example to verify 'encrypt' and 'decrypt' functions.
    - [ ] Don't use String for storing data, use something else like char[].
    - [ ] Make it compatible on other systems (mobile, etc.)
    > Use a different storing method, like encrypted JSON for better compatibility.
    - [ ] Backend API + Docker
    > For a much future implementation, introduce a way to send the data to a server rather than saving it on a local file.