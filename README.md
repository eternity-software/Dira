
<a name="readme-top"></a>

[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]
[![CC License][license-shield]][license-url]




<!-- PROJECT LOGO -->
<br />
<div align="center">
  <a href="https://github.com/eternity-software/Dira">
    <img src="https://i.imgur.com/zpaH5VM.png" alt="Logo" width="180" height="180">
  </a>

  <h3 align="center">Dira</h3>

  <p align="center">
    Native Android Client of New-Level Anonymous messenger
    <br />
    <a href="https://github.com/eternity-software/Dira/wiki"><strong>The docs are in progress »</strong></a> 
    <br />
    <br />
     <a href="https://t.me/diraapp">Telegram (Builds and News)</a>
    ·
    <a href="https://github.com/eternity-software/Dira/issues">Report Bug</a>
    ·
    <a href="https://github.com/eternity-software/Dira/issues">Request Feature</a>
  </p>
</div>



<!-- TABLE OF CONTENTS -->
<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
    </li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact">Contact</a></li>
  </ol>
</details>



<!-- ABOUT THE PROJECT -->
## About The Project

[![Dira App Screen Shot][product-screenshot]](https://diraapp.com)

`Dira is currently under development, but you still can try it.`

Dira is an open-source anonymous messenger that prioritizes privacy and data security. Unlike traditional messaging platforms, Dira does not require registration, accounts, or phone numbers. Each user has the freedom to create their own server and have full control over their data.

## Features

- **End-to-End Encryption**: Dira ensures the privacy and confidentiality of your conversations by leveraging basic end-to-end encryption. All chats (rooms) can be encrypted using end-to-end Diffie-Hellman encryption protocol, providing users with peace of mind.

- **Serverless Architecture**: Dira takes a decentralized approach, eliminating the need for centralized servers. Your data is not stored on any permanent server; instead, it remains securely stored on your device, giving you full ownership and control.

- **Every message type**: Dira supports almost all popular types of messages: voice messages, videos, images, even bubbles (circular video messages).

- **Fast**: Dira uses the best Android practices used in Telegram, Signal, etc. for being responsive and fast on every device

- **Content Sharing Flow**: Dira offers a seamless content sharing flow, allowing users to distribute various forms of content within the messenger. To create a flow, users need to obtain a secret `editing token`.

## Get it

You can download stable `.apk` from GitHub (check releases page) or from GooglePlay (now in progress)

Development builds are available in [our Telegram channel](https://t.me/diraapp) 

# How it works

[![Dira App Screen Shot][server-lifecycle]](https://diraapp.com)

Dira works on the basis of WebSockets and JSON. All server messages are requests and updates

<!-- GETTING STARTED -->

## Set-up

To start developing Dira on your Android device with Android Studio:

1. Clone the Dira repository or download the ZIP file.

2. Open Android Studio and select "Open an Existing Project".

3. Navigate to the location where you cloned or extracted the Dira repository and select the project folder.

4. Connect your Android device to your computer via USB and enable USB debugging in the device's developer options.

5. Click the "Run" button in Android Studio to build and run the Dira app on your connected Android device.

6. Follow the on-screen instructions to create or join a server.


Enjoy exploring Dira on Android with Android Studio!



<!-- CONTRIBUTING -->
## Contributing

Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

If you have a suggestion that would make this better, please fork the repo and create a pull request. You can also simply open an issue with the tag "enhancement".
Don't forget to give the project a star! Thanks again!

More you can find at [our contributing guide](https://github.com/eternity-software/Dira/blob/master/CONTRIBUTING.md)



<!-- LICENSE -->
## License

Distributed under the CC Non-commerical License. See `LICENSE` for more information.



<!-- CONTACT -->
## Contact

Contact us with [Official Discord Server](https://discord.gg/MDJ2jTgFCv)





<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->
[contributors-shield]: https://img.shields.io/github/contributors/eternity-software/Dira.svg?style=for-the-badge
[contributors-url]: https://github.com/eternity-software/Dira/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/eternity-software/Dira.svg?style=for-the-badge
[forks-url]: https://github.com/eternity-software/Dira/network/members
[stars-shield]: https://img.shields.io/github/stars/eternity-software/Dira.svg?style=for-the-badge
[stars-url]: https://github.com/eternity-software/Dira/stargazers
[issues-shield]: https://img.shields.io/github/issues/eternity-software/Dira.svg?style=for-the-badge
[issues-url]: https://github.com/eternity-software/Dira/issues
[license-shield]: https://img.shields.io/github/license/eternity-software/Dira.svg?style=for-the-badge
[license-url]: https://github.com/eternity-software/Dira/blob/master/LICENSE
[product-screenshot]: https://i.imgur.com/EukZItj.png
[server-lifecycle]: https://i.imgur.com/3yEcd49.png
