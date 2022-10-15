package nz.ac.auckland.se206.profiles;

public class ProfileHolder {
  private static ProfileHolder instance;

  /**
   * Gets the current instance or makes one if it doens't exist
   *
   * @return current instance
   */
  public static ProfileHolder getInstance() {
    if (instance == null) {
      instance = new ProfileHolder();
    }
    return instance;
  }

  private Profile currentProfile;

  private ProfileHolder() {}

  /**
   * sets the current profile to the one input
   *
   * @param profile
   */
  public void setCurrentProfile(Profile profile) {
    this.currentProfile = profile;
  }

  /**
   * gets the current profile that is stored
   *
   * @return
   */
  public Profile getCurrentProfile() {
    return this.currentProfile;
  }
}
