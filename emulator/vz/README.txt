Folder to store VZ-files, that can be read and written by
the OUT(port, n)-function:

PORT | IN / OUT | Description
-----|----------|-------------
252  | OUT      | LOAD .vz program no. [n]
253  | OUT      | SAVE .vz program no. [n]

Each file will be stored with this name corresponding to the given number:

	vzfile_nnn.vz

All programms with n <= 100 are readonly.
