# Reserved Keys and Configuration Properties

The following keys are reserved by Flinkboot. Do not use these names for your custom application configuration parameters or flags.

| Command Line Key | Environment Key | Description |
|------------------|-----------------|-------------|
| `-flinkboot-configurations` | `FLINKBOOT_CONFIGURATIONS` | Comma-separated list of configuration file paths/URIs to load and merge. |
| `--flinkboot-configuration-override` | `FLINKBOOT_CONFIGURATION_OVERRIDE` | Allows overriding configuration values in merged files instead of throwing an exception. |
| `--flinkboot-configuration-list-merging` | `FLINKBOOT_CONFIGURATION_LIST_MERGING` | Appends elements of lists/arrays together during merge instead of replacing them. |
