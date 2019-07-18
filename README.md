[![License MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

# Radix DLT Android Wallet

This branch of the Radix Android Wallet `release/1.0.0-beta` is the start of what is to come from our future public _BETANET_ release.

This wallet is a modification of the original wallet made especially to work with the _BETANET_ allowing for different types of tokens to be
sent and received as well as for easily creating new wallets with ease for either testing or development purposes.

The Radix DLT Android Wallet is making full use of the latest release of the [radixdlt-java](https://github.com/radixdlt/radixdlt-java/tree/release/1.0.0-beta) under the release/1.0.0-beta branch.

<img src="art/unlock_wallet.jpg" width="250">&nbsp;
<img src="art/transactions_screen.jpg" width="250">&nbsp;
<img src="art/contacts_screen.jpg" width="250">

# IMPORTANT NOTE

Please be aware that there will be a **v2** of the Radix wallet which will have a completely new design and features.  

For this reason, consider this branch to be used exclusively for testing and development purposes.

## Development setup

Use Android Studio 3.4.1 (or newer) to be able to build the app.

Bootstrap to your desired network by modifying the bootstrapConfig variable in the [RadixWalletApplication.kt](https://github.com/radixdlt/radixdlt-wallet-android/blob/release/1.0.0-beta/app/src/main/java/com/radixdlt/android/apps/wallet/RadixWalletApplication.kt) file.

The bootstrap variable will look like `private val bootstrapConfig = BootStrapConfigAndroidImpl.macAndroidEmulator(this)` where you can choose 3 options using the BootStrapConfigAndroidImpl.class.  

The 3 options are:

 * `BootStrapConfigAndroidImpl.radixBetanetNode(this)` (Used when connecting to the hosted radix betanet node)
 * `BootStrapConfigAndroidImpl.macAndroidEmulator(this)` (Used when connecting to a running localnode on your mac and also running the android emulator)
 
 * `BootStrapConfigAndroidImpl.localHost(this)` (Can take a String argument)
   - By default it will try to connect to "localhost" which will connect to your android emulator running on your Windows PC. This function can also take an argument as a String and can be an IP address or URL.  
     
   e.g.  
   `BootStrapConfigAndroidImpl.localHost(this, "192.168.0.2")` (`"192.168.0.2"` is the IP address given by the router to your computer to connect your physical android phone via LAN)  
   or  
   `BootStrapConfigAndroidImpl.localHost(this, "5.71.214.26")` (`"5.71.214.26"`is the IP address given by your ISP and you can connect to it as long as port 8080 has been forwarded on your router)

## Android development

 * Mostly written in [Kotlin](https://kotlinlang.org/)
 * Uses [Architecture Components](https://developer.android.com/topic/libraries/architecture/): Room, LiveData and Lifecycle-components
 * Uses [dagger-android](https://google.github.io/dagger/android.html) for dependency injection
 * Uses [RxJava 2](https://github.com/ReactiveX/RxJava) (Included by default by the radixdlt libs)
 

## Code style

This project uses [ktlint](https://github.com/shyiko/ktlint) via [Gradle](https://gradle.org/) dependency.
To check code style - `gradle ktlint` (it's also bound to `gradle check`).

## Contribute

Contributions are welcome, we simply ask to:

* Fork the codebase
* Make changes
* Submit a pull request for review

When contributing to this repository, we recommend discussing with the development team the change you wish to make using a [GitHub issue](https://github.com/radixdlt/radixdlt-wallet-android/issues) before making changes.

Please follow our [Code of Conduct](CODE_OF_CONDUCT.md) in all your interactions with the project.

## Links

| Link | Description |
| :----- | :------ |
[radixdlt.com](https://radixdlt.com/) | Radix DLT Homepage
[documentation](https://docs.radixdlt.com/) | Radix Knowledge Base
[forum](https://forum.radixdlt.com/) | Radix Technical Forum
[@radixdlt](https://twitter.com/radixdlt) | Follow Radix DLT on Twitter

## Have a question?

If you need any information, please visit our [GitHub Issues](https://github.com/radixdlt/radixdlt-wallet-android/issues) or the [Radix DLT android wallet #general channel](https://discord.gg/53G6eZU). Feel free to file an issue with as much information as possible about the problem.

## License

Radix DLT Android Wallet is released under the [MIT License](LICENSE).
