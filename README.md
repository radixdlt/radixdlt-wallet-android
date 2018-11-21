[![License MIT](https://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/radixdlt/radixdlt-wallet-android/blob/master/LICENSE)

Radix DLT Android Wallet
========================

The Radix DLT Android Wallet is currently working with the live ALPHANET and interacts with it
by making full use of the latest release of the [radixdlt-kotlin library](https://github.com/radixdlt/radixdlt-kotlin).

The App is a work in progress and will soon be migrated to using the new version of our network.

<img src="art/unlock_wallet.jpg" width="250">&nbsp;
<img src="art/transactions_screen.jpg" width="250">&nbsp;
<img  src="art/contacts_screen.jpg" width="250">

## Android development

 * Mostly written in [Kotlin](https://kotlinlang.org/) (A few classes classes are in Java but will be converted to keep it 100% Kotlin)
 * Uses [Architecture Components](https://developer.android.com/topic/libraries/architecture/): Room, LiveData and Lifecycle-components
 * Uses [dagger-android](https://google.github.io/dagger/android.html) for dependency injection
 * Uses [RxJava](https://github.com/ReactiveX/RxJava) 2 (Included by default by the radixdlt libs)

## Development setup

Use Android Studio 3.2.1 (or newer) to be able to build the app.

The app is currently using the latest release of the [radixdlt-kotlin library](https://github.com/radixdlt/radixdlt-kotlin) it is
possible to interchange this with the [radixdlt-java library](https://github.com/radixdlt/radixdlt-java) but you will need to target newer
android devices >= API 24. Also, a few simple changes in the code are necessary and the dependency
to [rxkotlin](https://github.com/ReactiveX/RxKotlin) must be included in your gradle file.

```
implementation(group: 'io.reactivex.rxjava2', name: 'rxkotlin', version: '2.2.0') {
    exclude group: 'io.reactivex.rxjava2', module: 'rxjava'
    exclude group: 'org.jetbrains.kotlin', module: 'kotlin-stdlib'
}
```

## Code style

This project uses [ktlint](https://github.com/shyiko/ktlint) via [Gradle](https://gradle.org/) dependency.
To check code style - `gradle ktlint` (it's also bound to `gradle check`).

## Contributing

Contributions are welcome, we simply ask to:

* Fork the codebase
* Make changes
* Submit a pull request for review

## Links

| Link | Description |
| :----- | :------ |
[radixdlt.com](https://radixdlt.com/) | Radix DLT Homepage
[documentation](https://docs.radixdlt.com/) | Radix Knowledge Base
[@radixdlt](https://twitter.com/radixdlt) | Follow Radix DLT on Twitter

## Have a question?

If you need any information, please visit our [GitHub Issues](https://github.com/radixdlt/radixdlt-wallet-android/issues) or the [Radix DLT android wallet #general channel](https://discord.gg/53G6eZU). Feel free to file an issue with as much information as possible about the problem.

## License

Radix DLT Android Wallet is released under the [MIT License](https://github.com/radixdlt/radixdlt-wallet-android/blob/master/LICENSE)
