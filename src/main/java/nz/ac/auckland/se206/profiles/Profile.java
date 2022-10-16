package nz.ac.auckland.se206.profiles;

import java.io.File;
import java.io.IOException;
import java.util.*;
import nz.ac.auckland.se206.badges.*;
import nz.ac.auckland.se206.games.Game;
import nz.ac.auckland.se206.games.Game.Difficulty;
import nz.ac.auckland.se206.games.Game.GameMode;
import nz.ac.auckland.se206.games.Game.Setting;

public class Profile {
  private String username;
  private int wins;
  private int losses;
  private int winStreak;
  private int fastestWinTime = 60;
  private Set<String> wordHistory = new HashSet<>();
  private List<Game> gameHistory = new ArrayList<Game>();
  private Map<Game.Setting, Game.Difficulty> setting2difficulty = new HashMap<>();
  private GameMode gameMode;
  // stores the names of all achieved badges (no information regarding rank)
  private List<Badge> badges = new ArrayList<>();

  public Profile(String username) throws IOException {
    this.username = username;
    // setting file path for profile under the profile directory in src folder
    String filePath = "profiles/" + username + ".json";
    File f = new File(filePath);
    if (f.exists()) {
      // get all required information
      Profile reading = ProfileLoader.read(username);
      this.wins = reading.getWins();
      this.losses = reading.getLosses();
      this.winStreak = reading.getWinStreak();
      this.fastestWinTime = reading.getFastestWinTime();
      this.wordHistory = reading.getWordHistory();
      this.gameHistory = reading.getGameHistory();
      this.setting2difficulty = reading.getSetting2Difficulty();
      this.gameMode = reading.getGameMode();
      this.badges = reading.getBadges();
    } else {
      // Add each badge instance
      badges.add(new FirstTryBadge());
      badges.add(new GrandmasterBadge());
      badges.add(new MasterBadge());
      badges.add(new SniperBadge());
      badges.add(new SprinterBadge());
      badges.add(new WinStreakBadge());
      badges.add(new WordCollectorBadge());
    }
  }

  public GameMode getGameMode() {
    return this.gameMode;
  }

  public void setGameMode(GameMode gameMode) {
    this.gameMode = gameMode;
  }

  /**
   * Gets the user name of the profile
   *
   * @return username
   */
  public String getUsername() {
    return username;
  }

  /**
   * Gets the wins of the profile
   *
   * @return number of wins
   */
  public int getWins() {
    return wins;
  }

  /**
   * Gets the losses of the profile
   *
   * @return number of losses
   */
  public int getLosses() {
    return losses;
  }

  /**
   * Gets the win streak of the profile
   *
   * @return win streak
   */
  public int getWinStreak() {
    return winStreak;
  }

  /**
   * Gets the fastest time the user has played the game in
   *
   * @return fastest time user has played
   */
  public int getFastestWinTime() {
    return this.fastestWinTime;
  }

  /**
   * Gets average time user has played
   *
   * @return
   */
  public int getAverageTime() {
    if (gameHistory.size() == 0) {
      return 60; // default value
    }

    int sum = 0;
    // sum up game times
    for (Game game : gameHistory) {
      sum += game.getDuration();
    }
    // calculate average
    return Math.round(sum / gameHistory.size());
  }

  /**
   * Gets the word history of the profile
   *
   * @return word history
   */
  public Set<String> getWordHistory() {
    return wordHistory;
  }

  /**
   * Gets the game history of the user
   *
   * @return game history
   */
  public List<Game> getGameHistory() {
    return gameHistory;
  }

  /**
   * Gets the game history of the user, but reversed
   *
   * @return reverse game history of user
   */
  public List<Game> getReversedGameHistory() {
    List<Game> reversed = new ArrayList<>(gameHistory);
    Collections.reverse(reversed);
    return reversed;
  }

  public Game getLatestGame() {
    return getReversedGameHistory().get(0);
  }

  /**
   * Get settings and difficulty of last game played
   *
   * @return
   */
  public Map<Setting, Difficulty> getSetting2Difficulty() {
    return setting2difficulty;
  }

  public List<Badge> getBadges() {
    return badges;
  }
  /**
   * Saves the current profile to a file of json format
   *
   * @throws IOException
   */
  public void saveToFile() throws IOException {
    ProfileLoader.updateJson(this);
  }

  /**
   * Checks to see if this is a guest profile or a user created one
   *
   * @return
   */
  public boolean isGuest() {
    return username.equals("Guest");
  }

  /**
   * Updates all profile statistics based on a new game result.
   *
   * @param game a finished game
   */
  public void updateAllStats(Game game) {
    // update wins/losses and win streak
    if (game.getIsWin()) {
      wins++;
      winStreak++;
    } else {
      losses++;
      winStreak = 0;
    }
    // update fastest wintime
    if (game.getDuration() < fastestWinTime) {
      fastestWinTime = game.getDuration();
    }
    // update word history
    wordHistory.add(game.getWord());
    // update game history
    gameHistory.add(game);
  }

  /** Deletes profile from system */
  public void delete() {
    String filePath = "profiles/" + username + ".json";
    File f = new File(filePath);
    if (f.exists()) {
      f.delete();
    }
  }
}
