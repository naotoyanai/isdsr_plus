# isdsr_plus
This is a repository of a secure routing protocol, named isdsr+, as behavior of the corresponding author of the following paper.
Authors: Hideharu Kojima, Naoto Yanai, Jason Paul Cruz  
Title: ISDSR+: Improving the Security and Availability of Secure Routing Protocol  
Journal: IEEE Access (Volume: 7 Page: 74849 - 74868)  
https://ieeexplore.ieee.org/document/8713519

We originally implemented and conducted exmperimtns on Raspberry Pi3 model B with Ubuntu 14.4 as OS and Java version of OpenJDK 1.8.1_151.

# How to Install
These codes work on jpbc library: http://gas.dia.unisa.it/projects/jpbc/

Install the jpbc library at first.  
Also build environment of the C language is necessary (because this is a Java wrapper provided by JPBC).  
Java security package is also required. (As described above, Java version of OpenJDK 1.8.1_151 is utilized.)  


# Role of Each Code
XXX.properties: Define parameters for bilinear maps  
class_diagram.jpg: flow of classes in source codes. (We used a.properties.)  

Each code is in src/ou/ist/de/srp.  
algo: directory for codes of the signature algorithms: IBSAS-DKG.  
node: directory for functions of nodes.  
packet: directory for packet configuration.  

Other implementation details have been described in Section 6 of our original paper. 
