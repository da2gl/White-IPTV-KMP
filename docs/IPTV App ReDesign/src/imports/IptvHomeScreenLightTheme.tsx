import clsx from "clsx";
import svgPaths from "./svg-e1eciwohyq";
import imgHboLogo from "figma:asset/6f5a7e666147805d36867ee84166e98397748de0.png";
import imgCnnLogo from "figma:asset/a72e1ab587480b31a6d94788d4f90e6b41df7221.png";
import imgEspnLogo from "figma:asset/835191cd2968fea38a63e4c3cf09d8611adb1361.png";
import imgDiscoveryLogo from "figma:asset/2594eb40e91876588354f5d877d61424ebdeda63.png";
import imgCinemaxLogo from "figma:asset/334a6414db26a21b95b812db3d4eb648b3f6296b.png";
import imgStarMoviesLogo from "figma:asset/10191fe1e30f6f7801c9df27f778f7bd89f4aa27.png";
import imgDisneyChannelLogo from "figma:asset/b15790b1368f76446fe2c8c4e7620dfc5eea0720.png";
import imgFxmLogo from "figma:asset/5d681e1a32a1577695215c4a0bccc6f9bf79dd94.png";
import imgBeInSportsLogo from "figma:asset/65e3b8c43e639dba4a8610db16ec08c9ea7b43e6.png";
import imgFoxSportsLogo from "figma:asset/cef1c0343dd6a4320ac69f2f40123ac60753d639.png";
import imgNbaTvLogo from "figma:asset/f61e77009bdef263b9b96e57dea7be906b93438e.png";

function Container1({ children }: React.PropsWithChildren<{}>) {
  return (
    <div className="h-[20px] relative shrink-0 w-[20.1px]">
      <svg className="absolute block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 20.1 20">
        <g id="Container">{children}</g>
      </svg>
    </div>
  );
}

function Container({ children }: React.PropsWithChildren<{}>) {
  return (
    <div className="relative shrink-0 size-[18px]">
      <svg className="absolute block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 18 18">
        <g id="Container">{children}</g>
      </svg>
    </div>
  );
}
type ContainerText1Props = {
  text: string;
};

function ContainerText1({ text }: ContainerText1Props) {
  return (
    <div className="content-stretch flex flex-col items-start relative shrink-0 w-full">
      <div className="flex flex-col font-['Inter:Semi_Bold',sans-serif] font-semibold justify-center leading-[0] not-italic relative shrink-0 text-[#2badee] text-[14px] w-full">
        <p className="leading-[21px]">{text}</p>
      </div>
    </div>
  );
}
type ContainerTextProps = {
  text: string;
};

function ContainerText({ text }: ContainerTextProps) {
  return (
    <div className="content-stretch flex flex-col items-start overflow-clip relative shrink-0 w-full">
      <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium justify-center leading-[0] not-italic relative shrink-0 text-[#212121] text-[16px] w-full">
        <p className="leading-[24px]">{text}</p>
      </div>
    </div>
  );
}
type TextProps = {
  text: string;
  additionalClassNames?: string;
};

function Text({ text, additionalClassNames = "" }: TextProps) {
  return (
    <div className={clsx("content-stretch flex flex-col items-start px-[16px] relative w-full", additionalClassNames)}>
      <div className="flex flex-col font-['Inter:Bold',sans-serif] font-bold justify-center leading-[0] not-italic relative shrink-0 text-[#212121] text-[22px] tracking-[-0.33px] w-full">
        <p className="leading-[27.5px]">{text}</p>
      </div>
    </div>
  );
}

export default function IptvHomeScreenLightTheme() {
  return (
    <div className="bg-[#f6f7f8] content-stretch flex flex-col isolate items-start relative size-full" data-name="IPTV Home Screen (Light Theme)">
      <div className="absolute backdrop-blur-[2px] bg-[rgba(246,247,248,0.8)] bottom-0 content-stretch flex flex-col items-start left-0 pt-px right-0 z-[3]" data-name="Bottom Navigation Bar">
        <div aria-hidden="true" className="absolute border-[#e5e7eb] border-solid border-t inset-0 pointer-events-none" />
        <div className="h-[80px] relative shrink-0 w-full" data-name="Container">
          <div className="flex flex-row items-center size-full">
            <div className="bg-clip-padding border-0 border-[transparent] border-solid content-stretch flex gap-[48.4px] items-center pl-[24.19px] pr-[24.2px] relative size-full">
              <div className="content-stretch flex flex-col gap-[4px] items-center justify-center relative shrink-0" data-name="Link">
                <div className="h-[18px] relative shrink-0 w-[16px]" data-name="Container">
                  <svg className="absolute block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 16 18">
                    <g id="Container">
                      <path d={svgPaths.p12a32500} fill="var(--fill-0, #2BADEE)" id="Icon" />
                    </g>
                  </svg>
                </div>
                <div className="content-stretch flex flex-col items-start relative shrink-0" data-name="Container">
                  <div className="flex flex-col font-['Inter:Semi_Bold',sans-serif] font-semibold h-[16px] justify-center leading-[0] not-italic relative shrink-0 text-[#2badee] text-[12px] w-[34.16px]">
                    <p className="leading-[16px]">Home</p>
                  </div>
                </div>
              </div>
              <div className="content-stretch flex flex-col gap-[4px] items-center justify-center relative shrink-0" data-name="Link">
                <div className="h-[18.35px] relative shrink-0 w-[20px]" data-name="Container">
                  <svg className="absolute block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 20 18.35">
                    <g id="Container">
                      <path d={svgPaths.p279a9400} fill="var(--fill-0, #6B7280)" id="Icon" />
                    </g>
                  </svg>
                </div>
                <div className="content-stretch flex flex-col items-start relative shrink-0" data-name="Container">
                  <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[16px] justify-center leading-[0] not-italic relative shrink-0 text-[#6b7280] text-[12px] w-[52.61px]">
                    <p className="leading-[16px]">Favorites</p>
                  </div>
                </div>
              </div>
              <div className="content-stretch flex flex-col gap-[4px] items-center justify-center relative shrink-0" data-name="Link">
                <Container>
                  <path d={svgPaths.p186f5ba0} fill="var(--fill-0, #6B7280)" id="Icon" />
                </Container>
                <div className="content-stretch flex flex-col items-start relative shrink-0" data-name="Container">
                  <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[16px] justify-center leading-[0] not-italic relative shrink-0 text-[#6b7280] text-[12px] w-[62.59px]">
                    <p className="leading-[16px]">Categories</p>
                  </div>
                </div>
              </div>
              <div className="content-stretch flex flex-col gap-[4px] items-center justify-center relative shrink-0" data-name="Link">
                <Container1>
                  <path d={svgPaths.p3cdadd00} fill="var(--fill-0, #6B7280)" id="Icon" />
                </Container1>
                <div className="content-stretch flex flex-col items-start relative shrink-0" data-name="Container">
                  <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[16px] justify-center leading-[0] not-italic relative shrink-0 text-[#6b7280] text-[12px] w-[47.08px]">
                    <p className="leading-[16px]">Settings</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div className="backdrop-blur-[2px] bg-[rgba(246,247,248,0.8)] relative shrink-0 w-full z-[2]" data-name="Top App Bar">
        <div className="flex flex-row items-center size-full">
          <div className="content-stretch flex items-center justify-between pb-[12px] pt-[16px] px-[16px] relative w-full">
            <div className="content-stretch flex gap-[4px] items-center relative shrink-0" data-name="Container">
              <div className="content-stretch flex flex-col items-start relative shrink-0" data-name="Heading 2">
                <div className="flex flex-col font-['Inter:Bold',sans-serif] font-bold h-[23px] justify-center leading-[0] not-italic relative shrink-0 text-[#212121] text-[18px] tracking-[-0.27px] w-[93.08px]">
                  <p className="leading-[22.5px]">My Playlist</p>
                </div>
              </div>
              <div className="h-[7.4px] relative shrink-0 w-[12px]" data-name="Container">
                <svg className="absolute block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 12 7.4">
                  <g id="Container">
                    <path d={svgPaths.p1adfde00} fill="var(--fill-0, #212121)" id="Icon" />
                  </g>
                </svg>
              </div>
            </div>
            <div className="content-stretch flex gap-[8px] items-center justify-end relative shrink-0" data-name="Container">
              <div className="content-stretch flex items-center justify-center relative rounded-[9999px] shrink-0 size-[40px]" data-name="Button">
                <Container>
                  <path d={svgPaths.p8a35e00} fill="var(--fill-0, #212121)" id="Icon" />
                </Container>
              </div>
              <div className="content-stretch flex items-center justify-center relative rounded-[9999px] shrink-0 size-[40px]" data-name="Button">
                <Container1>
                  <path d={svgPaths.p3cdadd00} fill="var(--fill-0, #212121)" id="Icon" />
                </Container1>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div className="content-stretch flex flex-col items-start pb-[96px] relative shrink-0 w-full z-[1]" data-name="Container">
        <div className="relative shrink-0 w-full" data-name="Heading 2 - Favorites Section">
          <Text text="Favorites" additionalClassNames="py-[12px]" />
        </div>
        <div className="h-[203px] overflow-clip relative shrink-0 w-full" data-name="Container">
          <div className="absolute bottom-0 content-stretch flex gap-[16px] items-start left-[16px] top-0" data-name="Container">
            <div className="content-stretch flex flex-col gap-[8px] h-full items-start min-w-[150px] relative rounded-[16px] shrink-0 w-[150px]" data-name="Container">
              <div className="bg-white content-stretch flex flex-col items-center justify-center py-[43px] relative rounded-[16px] shadow-[0px_1px_2px_0px_rgba(0,0,0,0.05)] shrink-0 w-full" data-name="Background+Shadow">
                <div className="max-w-[150px] relative shrink-0 size-[64px]" data-name="HBO Logo">
                  <div className="absolute inset-0 overflow-hidden pointer-events-none">
                    <img alt="" className="absolute left-0 max-w-none size-full top-0" src={imgHboLogo} />
                  </div>
                </div>
              </div>
              <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Container">
                <ContainerText text="HBO" />
                <ContainerText1 text="LIVE" />
              </div>
            </div>
            <div className="content-stretch flex flex-col gap-[8px] h-full items-start min-w-[150px] relative rounded-[16px] shrink-0 w-[150px]" data-name="Container">
              <div className="bg-white content-stretch flex flex-col items-center justify-center py-[51px] relative rounded-[16px] shadow-[0px_1px_2px_0px_rgba(0,0,0,0.05)] shrink-0 w-full" data-name="Background+Shadow">
                <div className="max-w-[150px] relative shrink-0 size-[48px]" data-name="CNN Logo">
                  <div className="absolute inset-0 overflow-hidden pointer-events-none">
                    <img alt="" className="absolute left-0 max-w-none size-full top-0" src={imgCnnLogo} />
                  </div>
                </div>
              </div>
              <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Container">
                <ContainerText text="CNN" />
                <ContainerText1 text="LIVE" />
              </div>
            </div>
            <div className="content-stretch flex flex-col gap-[8px] h-full items-start min-w-[150px] relative rounded-[16px] shrink-0 w-[150px]" data-name="Container">
              <div className="bg-white content-stretch flex flex-col items-center justify-center py-[47px] relative rounded-[16px] shadow-[0px_1px_2px_0px_rgba(0,0,0,0.05)] shrink-0 w-full" data-name="Background+Shadow">
                <div className="max-w-[150px] relative shrink-0 size-[56px]" data-name="ESPN Logo">
                  <div className="absolute inset-0 overflow-hidden pointer-events-none">
                    <img alt="" className="absolute left-0 max-w-none size-full top-0" src={imgEspnLogo} />
                  </div>
                </div>
              </div>
              <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Container">
                <ContainerText text="ESPN" />
                <ContainerText1 text="LIVE" />
              </div>
            </div>
            <div className="content-stretch flex flex-col gap-[8px] h-full items-start min-w-[150px] relative rounded-[16px] shrink-0 w-[150px]" data-name="Container">
              <div className="bg-white content-stretch flex flex-col items-center justify-center py-[55px] relative rounded-[16px] shadow-[0px_1px_2px_0px_rgba(0,0,0,0.05)] shrink-0 w-full" data-name="Background+Shadow">
                <div className="max-w-[150px] relative shrink-0 size-[40px]" data-name="Discovery Logo">
                  <div className="absolute inset-0 overflow-hidden pointer-events-none">
                    <img alt="" className="absolute left-0 max-w-none size-full top-0" src={imgDiscoveryLogo} />
                  </div>
                </div>
              </div>
              <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Container">
                <ContainerText text="Discovery" />
                <ContainerText1 text="LIVE" />
              </div>
            </div>
          </div>
        </div>
        <div className="relative shrink-0 w-full" data-name="Heading 2 - Movies Section">
          <Text text="Movies" additionalClassNames="pb-[12px] pt-[20px]" />
        </div>
        <div className="h-[203px] overflow-clip relative shrink-0 w-full" data-name="Container">
          <div className="absolute bottom-0 content-stretch flex gap-[16px] items-start left-[16px] top-0" data-name="Container">
            <div className="content-stretch flex flex-col gap-[8px] h-full items-start min-w-[150px] relative rounded-[16px] shrink-0 w-[150px]" data-name="Container">
              <div className="bg-white content-stretch flex flex-col items-center justify-center py-[59px] relative rounded-[16px] shadow-[0px_1px_2px_0px_rgba(0,0,0,0.05)] shrink-0 w-full" data-name="Background+Shadow">
                <div className="max-w-[150px] relative shrink-0 size-[32px]" data-name="Cinemax Logo">
                  <div className="absolute inset-0 overflow-hidden pointer-events-none">
                    <img alt="" className="absolute left-0 max-w-none size-full top-0" src={imgCinemaxLogo} />
                  </div>
                </div>
              </div>
              <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Container">
                <ContainerText text="Cinemax" />
                <ContainerText1 text="LIVE" />
              </div>
            </div>
            <div className="content-stretch flex flex-col gap-[8px] h-full items-start min-w-[150px] relative rounded-[16px] shrink-0 w-[150px]" data-name="Container">
              <div className="bg-white content-stretch flex flex-col items-center justify-center py-[43px] relative rounded-[16px] shadow-[0px_1px_2px_0px_rgba(0,0,0,0.05)] shrink-0 w-full" data-name="Background+Shadow">
                <div className="max-w-[150px] relative shrink-0 size-[64px]" data-name="Star Movies Logo">
                  <div className="absolute inset-0 overflow-hidden pointer-events-none">
                    <img alt="" className="absolute left-0 max-w-none size-full top-0" src={imgStarMoviesLogo} />
                  </div>
                </div>
              </div>
              <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Container">
                <ContainerText text="Star Movies" />
                <ContainerText1 text="LIVE" />
              </div>
            </div>
            <div className="content-stretch flex flex-col gap-[8px] h-full items-start min-w-[150px] relative rounded-[16px] shrink-0 w-[150px]" data-name="Container">
              <div className="bg-white content-stretch flex flex-col items-center justify-center py-[43px] relative rounded-[16px] shadow-[0px_1px_2px_0px_rgba(0,0,0,0.05)] shrink-0 w-full" data-name="Background+Shadow">
                <div className="max-w-[150px] relative shrink-0 size-[64px]" data-name="Disney Channel Logo">
                  <div className="absolute inset-0 overflow-hidden pointer-events-none">
                    <img alt="" className="absolute left-0 max-w-none size-full top-0" src={imgDisneyChannelLogo} />
                  </div>
                </div>
              </div>
              <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Container">
                <ContainerText text="Disney Channel" />
                <ContainerText1 text="LIVE" />
              </div>
            </div>
            <div className="content-stretch flex flex-col gap-[8px] h-full items-start min-w-[150px] relative rounded-[16px] shrink-0 w-[150px]" data-name="Container">
              <div className="bg-white content-stretch flex flex-col items-center justify-center py-[55px] relative rounded-[16px] shadow-[0px_1px_2px_0px_rgba(0,0,0,0.05)] shrink-0 w-full" data-name="Background+Shadow">
                <div className="max-w-[150px] relative shrink-0 size-[40px]" data-name="FXM Logo">
                  <div className="absolute inset-0 overflow-hidden pointer-events-none">
                    <img alt="" className="absolute left-0 max-w-none size-full top-0" src={imgFxmLogo} />
                  </div>
                </div>
              </div>
              <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Container">
                <ContainerText text="FXM" />
                <ContainerText1 text="LIVE" />
              </div>
            </div>
          </div>
        </div>
        <div className="relative shrink-0 w-full" data-name="Heading 2 - Sports Section">
          <Text text="Sports" additionalClassNames="pb-[12px] pt-[20px]" />
        </div>
        <div className="h-[203px] overflow-clip relative shrink-0 w-full" data-name="Container">
          <div className="absolute bottom-0 content-stretch flex gap-[16px] items-start left-[16px] top-0" data-name="Container">
            <div className="content-stretch flex flex-col gap-[8px] h-full items-start min-w-[150px] relative rounded-[16px] shrink-0 w-[150px]" data-name="Container">
              <div className="bg-white content-stretch flex flex-col items-center justify-center py-[43px] relative rounded-[16px] shadow-[0px_1px_2px_0px_rgba(0,0,0,0.05)] shrink-0 w-full" data-name="Background+Shadow">
                <div className="max-w-[150px] relative shrink-0 size-[64px]" data-name="beIN Sports Logo">
                  <div className="absolute inset-0 overflow-hidden pointer-events-none">
                    <img alt="" className="absolute left-0 max-w-none size-full top-0" src={imgBeInSportsLogo} />
                  </div>
                </div>
              </div>
              <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Container">
                <ContainerText text="beIN Sports" />
                <ContainerText1 text="LIVE" />
              </div>
            </div>
            <div className="content-stretch flex flex-col gap-[8px] h-full items-start min-w-[150px] relative rounded-[16px] shrink-0 w-[150px]" data-name="Container">
              <div className="bg-white content-stretch flex flex-col items-center justify-center py-[47px] relative rounded-[16px] shadow-[0px_1px_2px_0px_rgba(0,0,0,0.05)] shrink-0 w-full" data-name="Background+Shadow">
                <div className="max-w-[150px] relative shrink-0 size-[56px]" data-name="Fox Sports Logo">
                  <div className="absolute inset-0 overflow-hidden pointer-events-none">
                    <img alt="" className="absolute left-0 max-w-none size-full top-0" src={imgFoxSportsLogo} />
                  </div>
                </div>
              </div>
              <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Container">
                <ContainerText text="Fox Sports" />
                <ContainerText1 text="LIVE" />
              </div>
            </div>
            <div className="content-stretch flex flex-col gap-[8px] h-full items-start min-w-[150px] relative rounded-[16px] shrink-0 w-[150px]" data-name="Container">
              <div className="bg-white content-stretch flex flex-col items-center justify-center py-[47px] relative rounded-[16px] shadow-[0px_1px_2px_0px_rgba(0,0,0,0.05)] shrink-0 w-full" data-name="Background+Shadow">
                <div className="max-w-[150px] relative shrink-0 size-[56px]" data-name="NBA TV Logo">
                  <div className="absolute inset-0 overflow-hidden pointer-events-none">
                    <img alt="" className="absolute left-0 max-w-none size-full top-0" src={imgNbaTvLogo} />
                  </div>
                </div>
              </div>
              <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Container">
                <ContainerText text="NBA TV" />
                <ContainerText1 text="LIVE" />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}