:: This script copies the generated runtime components from the
:: (external) GIPS MdVNE example folder into this repository.

setlocal

set MDVNE_PROJECT_NAME=org.emoflon.gips.gipsl.examples.mdvne

rmdir ..\%MDVNE_PROJECT_NAME%\src-gen /s /q
xcopy ..\..\gips-examples\%MDVNE_PROJECT_NAME%\src-gen ..\%MDVNE_PROJECT_NAME%\src-gen\ /e

rmdir ..\%MDVNE_PROJECT_NAME%.migration\src-gen /s /q
xcopy ..\..\gips-examples\%MDVNE_PROJECT_NAME%.migration\src-gen ..\%MDVNE_PROJECT_NAME%.migration\src-gen\ /e

rmdir ..\%MDVNE_PROJECT_NAME%.seq\src-gen /s /q
xcopy ..\..\gips-examples\%MDVNE_PROJECT_NAME%.seq\src-gen ..\%MDVNE_PROJECT_NAME%.seq\src-gen\ /e

rmdir ..\%MDVNE_PROJECT_NAME%.bwignore\src-gen /s /q
xcopy ..\..\gips-examples\%MDVNE_PROJECT_NAME%.bwignore\src-gen ..\%MDVNE_PROJECT_NAME%.bwignore\src-gen\ /e

endlocal