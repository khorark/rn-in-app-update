# rn-in-app-update

Package for in-app update your app

## Description
Android update use native SDK for in-app update.
## Installation

```sh
npm install @khorark/rn-in-app-update --save

cd ios && pod install
```

## Usage

Use hook for check update

```js
import { useInAppUpdate } from '@khorark/rn-in-app-update';

// ...

const App = () => {
  useInAppUpdate();

  return <MainComponent />;
};
```

Use func for check update

```js
// ...
import { checkUpdate } from '@khorark/rn-in-app-update';
// ...

const App = () => {
  return <Pressable onPress={checkUpdate}>/* ... */ </Pressable>;
};
```

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
