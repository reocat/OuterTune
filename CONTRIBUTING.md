# Building
For most users, we recommend importing and building through Android Studio.

## Build variants
There are the following build variants
```
universal (all architectures)
arm64 (arm64-v8a)
uncommonabi (armeabi-v7a, x86, x86_64)
x86_64
```
**For most users, the `universal` variant is sufficient.** The other build varients may reduce file size, however at the cost of compatibility.



<br/><br/>

# Contributing to OuterTune

## Submitting a pull request
- One pull request for one feature/issue, do not tackle unrelated features/issues in one pull request
- Write a descriptive title and a meaningful description
- Upload images/video for any UI changes
- In the event of merge conflicts, you may be required to rebase onto the current `dev` branch
- **You are required to build and test the app before submitting a pull request**

## Translations

Follow the [instructions](https://developer.android.com/guide/topics/resources/localization) and
create a pull request. **You are also required to build the app beforehand** and make sure there is no error
before you create a pull request.