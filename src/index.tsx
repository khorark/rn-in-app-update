import { useEffect } from 'react';
import { NativeModules, Platform } from 'react-native';
import { appStoreInApp } from './utils/appStoreInApp';

const LINKING_ERROR =
  `The package 'rn-in-app-update' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const RnInAppUpdate = NativeModules.RnInAppUpdate
  ? NativeModules.RnInAppUpdate
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export function checkUpdate(): void {
  switch (Platform.OS) {
    case 'android':
      RnInAppUpdate.checkUpdate();
      break;
    case 'ios':
      appStoreInApp.checkUpdate();
      break;
  }
}

export const useInAppUpdate = () => {
  useEffect(() => {
    switch (Platform.OS) {
      case 'android':
        RnInAppUpdate.checkUpdate();
        break;
      case 'ios':
        appStoreInApp.checkUpdate();
        break;
    }
  }, []);
};
