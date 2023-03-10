package nz.ac.auckland.se206.controllers;

import com.opencsv.exceptions.CsvException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import nz.ac.auckland.se206.SceneManager;
import nz.ac.auckland.se206.SceneManager.AppUi;
import nz.ac.auckland.se206.dictionary.DictionaryLookUp;
import nz.ac.auckland.se206.games.Game.Difficulty;
import nz.ac.auckland.se206.games.Game.GameMode;
import nz.ac.auckland.se206.games.Game.Setting;
import nz.ac.auckland.se206.profiles.Profile;
import nz.ac.auckland.se206.profiles.ProfileHolder;
import nz.ac.auckland.se206.words.CategorySelector;
import nz.ac.auckland.se206.words.WordHolder;

public class DifficultySelectorController implements Initializable, SwitchInListener {
  @FXML private Spinner<String> modeSpinner;
  @FXML private Spinner<String> accuracySpinner;
  @FXML private Spinner<String> wordsSpinner;
  @FXML private Spinner<String> timeSpinner;
  @FXML private Spinner<String> confidenceSpinner;
  @FXML private HBox accuracyContainer;
  @FXML private HBox timeContainer;
  @FXML private HBox confidenceContainer;
  @FXML private Button chooseDifficultyButton;
  @FXML private AnchorPane loadingContainer;
  private CategorySelector categorySelector;

  /**
   * called when JavaFx is done loading all GUI, this method grabs all the available categories and
   * stores them in a HashMap for later use
   */
  @Override
  public void initialize(URL location, ResourceBundle resources) {
    // create a new instance of a category selector
    try {
      categorySelector = new CategorySelector();
    } catch (IOException | CsvException | URISyntaxException e) {
      e.printStackTrace();
    }

    // Initialize mode spinner
    ObservableList<String> gameModes =
        FXCollections.observableArrayList("NORMAL", "HIDDEN", "ZEN", "SCRAMBLE");
    initializeSpinner(modeSpinner, gameModes);

    // When selecting Zen Mode, only word difficulty shows up
    modeSpinner
        .getValueFactory()
        .valueProperty()
        .addListener(
            (obs, oldValue, newValue) -> {
              // show only difficulty
              if (newValue.equals("ZEN")) {
                accuracyContainer.setVisible(false);
                timeContainer.setVisible(false);
                confidenceContainer.setVisible(false);
              } else {
                // show all options
                accuracyContainer.setVisible(true);
                timeContainer.setVisible(true);
                confidenceContainer.setVisible(true);
              }
            });

    // Initialize spinners with "Easy", "Medium", and "Hard" difficulties
    ObservableList<String> accuracyDifficulties =
        FXCollections.observableArrayList("SUPER EASY", "EASY", "MEDIUM", "HARD");
    initializeSpinner(accuracySpinner, accuracyDifficulties);

    ObservableList<String> timeDifficulties =
        FXCollections.observableArrayList("SUPER EASY", "EASY", "MEDIUM", "HARD", "MASTER");
    initializeSpinner(timeSpinner, timeDifficulties);

    // Initialize spinners with "Easy", "Medium", "Hard", and "Master" difficulties
    ObservableList<String> difficulties =
        FXCollections.observableArrayList("EASY", "MEDIUM", "HARD", "MASTER");
    initializeSpinner(wordsSpinner, difficulties);
    initializeSpinner(confidenceSpinner, difficulties);
  }

  /**
   * Initializes a spinner with relevant difficulties for user selection
   *
   * @param spinner a spinner on the GUI that the user uses to select the relevant difficulty
   * @param options all available option strings for the setting
   */
  private void initializeSpinner(Spinner<String> spinner, ObservableList<String> options) {
    SpinnerValueFactory<String> valueFactory =
        new SpinnerValueFactory.ListSpinnerValueFactory<String>(options);
    valueFactory.setValue(options.get(0));
    spinner.setValueFactory(valueFactory);
    spinner.editorProperty().get().setAlignment(Pos.CENTER);
  }

  /**
   * Sets the users chosen difficulty settings Triggers when ChooseDifficultyButton is pressed
   *
   * @param event the event of triggering this method
   * @throws IOException if there was an error reading or writing to a file
   */
  @FXML
  private void onChooseDifficulty(ActionEvent event1) throws IOException {
    // Let the user know that the game is loading and turn off selectors
    loadingContainer.setVisible(true);
    disableForLoad(true);
    // run in new thread as it takes a while
    Task<Void> backgroundTask =
        new Task<>() {
          @Override
          protected Void call() throws Exception {
            // all difficulty options
            setGameMode();
            setAccuracyDifficulty();
            setWordsDifficulty();
            setTimeDifficulty();
            setConfidenceDifficulty();
            Platform.runLater(
                () -> {
                  // change to correct scene
                  if (modeSpinner.getValue().equals("HIDDEN")) {
                    SceneManager.changeScene(event1, AppUi.CATEGORY_DISPLAY_HIDDEN);
                  } else {
                    SceneManager.changeScene(event1, AppUi.CATEGORY_DISPLAY);
                  }
                });
            return null;
          }
        };
    // run thread
    Thread backgroundPerson = new Thread(backgroundTask);
    backgroundPerson.start();
  }

  /**
   * Enable or disable spinners depending on if the game is loading or not
   *
   * @param disable toggle if the spinners are visible or not
   */
  private void disableForLoad(boolean disable) {
    // toggle if the spinners are disabled or not
    modeSpinner.setDisable(disable);
    accuracySpinner.setDisable(disable);
    wordsSpinner.setDisable(disable);
    timeSpinner.setDisable(disable);
    confidenceSpinner.setDisable(disable);
    // toggle the play button
    chooseDifficultyButton.setDisable(disable);
  }

  /** Set the selected game mode to the profile of the current user */
  private void setGameMode() {
    if (modeSpinner.getValue().equals("NORMAL")) {
      // Normal gamemode selected
      ProfileHolder.getInstance().getCurrentProfile().setGameMode(GameMode.NORMAL);
    } else if (modeSpinner.getValue().equals("HIDDEN")) {
      // Hidden word gamemode selected
      ProfileHolder.getInstance().getCurrentProfile().setGameMode(GameMode.HIDDEN);
    } else if (modeSpinner.getValue().equals("SCRAMBLE")) {
      // Scramble game mode
      ProfileHolder.getInstance().getCurrentProfile().setGameMode(GameMode.SCRAMBLE);
    } else {
      // Zen gamemode selected
      ProfileHolder.getInstance().getCurrentProfile().setGameMode(GameMode.ZEN);
    }
  }

  /** Sets the next game's accuracy cut off as per the difficulty selected by accuracy spinner */
  private void setAccuracyDifficulty() {
    if (accuracySpinner.getValue().equals("SUPER EASY")) {
      // Super easy difficulty selected
      ProfileHolder.getInstance()
          .getCurrentProfile()
          .getSetting2Difficulty()
          .put(Setting.ACCURACY, Difficulty.SUPER_EASY);
    } else if (accuracySpinner.getValue().equals("EASY")) {
      // Easy difficulty
      ProfileHolder.getInstance()
          .getCurrentProfile()
          .getSetting2Difficulty()
          .put(Setting.ACCURACY, Difficulty.EASY);
    } else if (accuracySpinner.getValue().equals("MEDIUM")) {
      // Medium difficulty selected
      ProfileHolder.getInstance()
          .getCurrentProfile()
          .getSetting2Difficulty()
          .put(Setting.ACCURACY, Difficulty.MEDIUM);
    } else { // Hard difficulty selected
      ProfileHolder.getInstance()
          .getCurrentProfile()
          .getSetting2Difficulty()
          .put(Setting.ACCURACY, Difficulty.HARD);
    }
  }

  /** Sets the next game's word as per the difficulty selected by words spinner */
  private void setWordsDifficulty() {
    if (ProfileHolder.getInstance().getCurrentProfile().getGameMode() == GameMode.SCRAMBLE) {
      if (wordsSpinner.getValue().equals("EASY")) {
        // Easy difficulty selected
        WordHolder.getInstance().setCurrentWord(categorySelector.getEasyCategorySingleWord());
        ProfileHolder.getInstance()
            .getCurrentProfile()
            .getSetting2Difficulty()
            .put(Setting.WORDS, Difficulty.EASY);
      } else if (wordsSpinner.getValue().equals("MEDIUM")) {
        // Medium difficulty selected
        WordHolder.getInstance().setCurrentWord(categorySelector.getMediumCategorySingleWord());
        ProfileHolder.getInstance()
            .getCurrentProfile()
            .getSetting2Difficulty()
            .put(Setting.WORDS, Difficulty.MEDIUM);
      } else if (wordsSpinner.getValue().equals("HARD")) {
        // Hard difficulty selected
        WordHolder.getInstance().setCurrentWord(categorySelector.getHardCategorySingleWord());
        ProfileHolder.getInstance()
            .getCurrentProfile()
            .getSetting2Difficulty()
            .put(Setting.WORDS, Difficulty.HARD);
      } else {
        // Master difficulty selected
        WordHolder.getInstance().setCurrentWord(categorySelector.getMasterCategorySingleWord());
        ProfileHolder.getInstance()
            .getCurrentProfile()
            .getSetting2Difficulty()
            .put(Setting.WORDS, Difficulty.MASTER);
      }
    } else {
      if (wordsSpinner.getValue().equals("EASY")) {
        // Easy difficulty selected
        WordHolder.getInstance().setCurrentWord(categorySelector.getEasyCategory());
        ProfileHolder.getInstance()
            .getCurrentProfile()
            .getSetting2Difficulty()
            .put(Setting.WORDS, Difficulty.EASY);
      } else if (wordsSpinner.getValue().equals("MEDIUM")) {
        // Medium difficulty selected
        WordHolder.getInstance().setCurrentWord(categorySelector.getMediumCategory());
        ProfileHolder.getInstance()
            .getCurrentProfile()
            .getSetting2Difficulty()
            .put(Setting.WORDS, Difficulty.MEDIUM);
      } else if (wordsSpinner.getValue().equals("HARD")) {
        // Hard difficulty selected
        WordHolder.getInstance().setCurrentWord(categorySelector.getHardCategory());
        ProfileHolder.getInstance()
            .getCurrentProfile()
            .getSetting2Difficulty()
            .put(Setting.WORDS, Difficulty.HARD);
      } else {
        // Master difficulty selected
        WordHolder.getInstance().setCurrentWord(categorySelector.getMasterCategory());
        ProfileHolder.getInstance()
            .getCurrentProfile()
            .getSetting2Difficulty()
            .put(Setting.WORDS, Difficulty.MASTER);
      }
    }

    // if no dictionary entry for word in hidden mode, find another word
    try {
      if (modeSpinner.getValue().equals("HIDDEN")
          && DictionaryLookUp.searchWordInfo(WordHolder.getInstance().getCurrentWord()) == null) {
        // update word until one that works is found
        setWordsDifficulty();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /** Sets the time as per the difficulty selected by time spinner */
  private void setTimeDifficulty() {
    if (timeSpinner.getValue().equals("SUPER EASY")) {
      // Super easy difficulty selected
      ProfileHolder.getInstance()
          .getCurrentProfile()
          .getSetting2Difficulty()
          .put(Setting.TIME, Difficulty.SUPER_EASY);
    } else if (timeSpinner.getValue().equals("EASY")) {
      // Easy difficulty selected
      ProfileHolder.getInstance()
          .getCurrentProfile()
          .getSetting2Difficulty()
          .put(Setting.TIME, Difficulty.EASY);
    } else if (timeSpinner.getValue().equals("MEDIUM")) {
      // Medium difficulty selected
      ProfileHolder.getInstance()
          .getCurrentProfile()
          .getSetting2Difficulty()
          .put(Setting.TIME, Difficulty.MEDIUM);
    } else if (timeSpinner.getValue().equals("HARD")) {
      // Hard difficulty selected
      ProfileHolder.getInstance()
          .getCurrentProfile()
          .getSetting2Difficulty()
          .put(Setting.TIME, Difficulty.HARD);
    } else {
      // Master difficulty selected
      ProfileHolder.getInstance()
          .getCurrentProfile()
          .getSetting2Difficulty()
          .put(Setting.TIME, Difficulty.MASTER);
    }
  }

  /**
   * Sets the next game's confidence cut off as per the difficulty selected by confidence spinner
   */
  private void setConfidenceDifficulty() {
    if (confidenceSpinner.getValue().equals("EASY")) {
      // Easy difficulty selected
      ProfileHolder.getInstance()
          .getCurrentProfile()
          .getSetting2Difficulty()
          .put(Setting.CONFIDENCE, Difficulty.EASY);
    } else if (confidenceSpinner.getValue().equals("MEDIUM")) {
      // Medium difficulty selected
      ProfileHolder.getInstance()
          .getCurrentProfile()
          .getSetting2Difficulty()
          .put(Setting.CONFIDENCE, Difficulty.MEDIUM);
    } else if (confidenceSpinner.getValue().equals("HARD")) {
      // Hard difficulty selected
      ProfileHolder.getInstance()
          .getCurrentProfile()
          .getSetting2Difficulty()
          .put(Setting.CONFIDENCE, Difficulty.HARD);
    } else {
      // Master difficulty selected
      ProfileHolder.getInstance()
          .getCurrentProfile()
          .getSetting2Difficulty()
          .put(Setting.CONFIDENCE, Difficulty.MASTER);
    }
  }

  /** Updates the spinner value being displayed as per the profiles previous selection */
  public void setSpinners() {
    Profile profile = ProfileHolder.getInstance().getCurrentProfile();
    GameMode gameMode = profile.getGameMode();
    Map<Setting, Difficulty> map = profile.getSetting2Difficulty();
    // if profile sees spinners for the first time
    if (gameMode == null) {
      // update mode
      modeSpinner.getValueFactory().setValue("NORMAL");
      // update difficulties
      accuracySpinner.getValueFactory().setValue("EASY");
      wordsSpinner.getValueFactory().setValue("EASY");
      timeSpinner.getValueFactory().setValue("EASY");
      confidenceSpinner.getValueFactory().setValue("EASY");
    } else {
      // if profile has had a previous selection using the spinners
      // update mode
      modeSpinner.getValueFactory().setValue(gameMode.toString());
      // update difficulties
      accuracySpinner
          .getValueFactory()
          .setValue(map.get(Setting.ACCURACY).toString().replace("_", " "));
      wordsSpinner.getValueFactory().setValue(map.get(Setting.WORDS).toString());
      timeSpinner.getValueFactory().setValue(map.get(Setting.TIME).toString().replace("_", " "));
      confidenceSpinner.getValueFactory().setValue(map.get(Setting.CONFIDENCE).toString());
    }
  }

  /** sets the spinners to the correct value required */
  @Override
  public void onSwitchIn() {
    loadingContainer.setVisible(false);
    disableForLoad(false);
    setSpinners();
  }
}
