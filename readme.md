# Jump Continue

Jump Continue is a web site for allowing for the continued reuse of jumpstart theme packs, via allowing randomisation or selections of themes out of available packs.
The project is composed of:

* A set of Java processes for preparing website assets
* The site itself, as pure html5/css/javascript using jquery.

## Getting Started

### Prerequisites

Java 11

### Installing

Build the assets with:

```gradlew.bat run```

Run the site locally with something like Web Server for Chrome, or upload in full to some location.

## Further Information

Input folder contains all the input needed to build the site - grouping, acquisition metadata and guaranteed wardrobe unlock preview screenshots.
Cache folder contains the intermediate information generated by the site builder, including cached results from the Guild Wars 2 API and Wiki.
Site contains the website itself. Building the site creates a content.json file that contains information on all the unlocks available, and one or more image maps of the unlock icons.

## Authors

* **Immortius**

## License

All code is released under Apache 2.0.
