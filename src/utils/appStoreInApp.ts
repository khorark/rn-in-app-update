import { Alert, Linking } from 'react-native';
import DeviceInfo from 'react-native-device-info';

type ITunesData = {
  screenshotUrls: string[];
  ipadScreenshotUrls: string[];
  appletvScreenshotUrls: string[];
  artworkUrl60: string;
  artworkUrl512: string;
  artworkUrl100: string;
  artistViewUrl: string;
  supportedDevices: string[];
  advisories: string[];
  isGameCenterEnabled: string[];
  features: string[];
  kind: string;
  trackCensoredName: string;
  languageCodesISO2A: string[];
  fileSizeBytes: string;
  contentAdvisoryRating: string;
  averageUserRatingForCurrentVersion: number;
  userRatingCountForCurrentVersion: number;
  averageUserRating: number;
  trackViewUrl: string;
  trackContentRating: string;
  isVppDeviceBasedLicensingEnabled: boolean;
  trackId: number;
  trackName: string;
  releaseDate: string;
  genreIds: string[];
  formattedPrice: string;
  primaryGenreName: string;
  minimumOsVersion: string;
  currentVersionReleaseDate: string;
  releaseNotes: string;
  primaryGenreId: number;
  sellerName: string;
  currency: string;
  description: string;
  artistId: number;
  artistName: string;
  genres: string[];
  price: number;
  bundleId: string;
  version: string;
  wrapperType: string;
  userRatingCount: number;
};

type ITunesResponse = {
  resultCount: number;
  results: ITunesData[];
};

type PromptUserOptions = {
  title: string;
  message?: string;
  buttonUpgradeText?: string;
  buttonCancelText?: string;
};

type ShowUpgradePromptParams = {
  appId: string;
  options: PromptUserOptions;
};

interface AppStoreInApp {
  checkUpdate: () => Promise<void>;
}

class AppStoreInAppImpl implements AppStoreInApp {
  private async getLatestData(bundleId: string): Promise<ITunesData | null> {
    try {
      const response = await fetch(
        `https://itunes.apple.com/?bundleId=${bundleId}`,
        {
          headers: {
            'Cache-Control': 'no-cache',
            'Content-Type': 'application/json',
            'Accept': 'application/json',
          },
        }
      );

      const data: ITunesResponse = await response.json();

      if (data) {
        const latestInfo = data.results[0];
        if (latestInfo) {
          return latestInfo;
        }
      }

      return null;
    } catch (e) {
      return null;
    }
  }

  private attemptUpgrade = (appId: string): void => {
    // failover if itunes - a bit excessive
    const appStoreURI = `itms-apps://apps.apple.com/app/id${appId}?mt=8`;
    const appStoreURL = `https://apps.apple.com/app/id${appId}?mt=8`;

    Linking.canOpenURL(appStoreURI).then((supported) => {
      if (supported) {
        Linking.openURL(appStoreURI);
      } else {
        Linking.openURL(appStoreURL);
      }
    });
  };

  private showUpgradePrompt = ({
    appId,
    options: { title, message, buttonUpgradeText, buttonCancelText },
  }: ShowUpgradePromptParams) => {
    Alert.alert(
      title,
      message,
      [
        {
          text: buttonUpgradeText,
          onPress: () => this.attemptUpgrade(appId),
        },
        { text: buttonCancelText },
      ],
      { cancelable: true }
    );
  };

  public async checkUpdate() {
    const iTunesInfo = await this.getLatestData(DeviceInfo.getBundleId());
    if (iTunesInfo && iTunesInfo.version > DeviceInfo.getVersion()) {
      this.showUpgradePrompt({
        appId: iTunesInfo.trackId.toString(),
        options: {
          title: 'Update Available',
          message:
            'There is an updated version available on the App Store. Would you like to upgrade?',
          buttonUpgradeText: 'Upgrade',
          buttonCancelText: 'Cancel',
        },
      });
    }
  }
}

export const appStoreInApp = new AppStoreInAppImpl();
