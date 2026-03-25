import clsx from "clsx";
import svgPaths from "./svg-agd849qoos";
import imgBackground from "figma:asset/94eb0a08152b1b737f4d7b5b79e3d20d003d04de.png";
import imgBackground1 from "figma:asset/dabdae8ba34a84ac0abc10dbceb097953d93407b.png";
import imgBackground2 from "figma:asset/865d1344faaf45711c7d6af8524af5b392861638.png";
import imgBackground3 from "figma:asset/54a463f2646cec1ad59a6a8535d3cd3acc040a21.png";
import imgBackground4 from "figma:asset/3569e195133bb89445836081203f9a3eefb378a9.png";
import imgBackground5 from "figma:asset/8357fbaffbb734e010c835806f447b0851b9db2b.png";
import imgBackground6 from "figma:asset/5541af6f396a5d8be085145f0d6dbd34091da737.png";
import imgBackground7 from "figma:asset/9539bf0c747e5dd24d5a6bd654edde1aa1152b45.png";
import imgBackground8 from "figma:asset/7d33bda6af9f879aee2604f0160e67d70afe49a4.png";
import imgBackground9 from "figma:asset/4cfdcbe7beae54c979e3f28626452510ac2b72f8.png";

function Background({ children }: React.PropsWithChildren<{}>) {
  return (
    <div className="relative rounded-[8px] shrink-0 w-full">
      <div className="absolute inset-0 overflow-hidden pointer-events-none rounded-[8px]">{children}</div>
      <div className="flex flex-col justify-end size-full">
        <div className="content-stretch flex flex-col items-start justify-end pb-[8px] pt-[132px] px-[8px] relative w-full">
          <div className="backdrop-blur-[2px] bg-[rgba(43,173,238,0.2)] content-stretch flex flex-col items-start px-[8px] py-[2px] relative rounded-[9999px] shrink-0">
            <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[16px] justify-center leading-[0] not-italic relative shrink-0 text-[#2badee] text-[12px] w-[25.81px]">
              <p className="leading-[16px]">{"LIVE"}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

function Container1({ children }: React.PropsWithChildren<{}>) {
  return (
    <div className="relative shrink-0 w-full">
      <div className="content-stretch flex flex-col items-start p-[8px] relative w-full">{children}</div>
    </div>
  );
}

function Container({ children }: React.PropsWithChildren<{}>) {
  return (
    <div className="h-[20px] relative shrink-0 w-[20.1px]">
      <svg className="absolute block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 20.1 20">
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
      <div className="flex flex-col font-['Inter:Regular',sans-serif] font-normal justify-center leading-[0] not-italic relative shrink-0 text-[#94a3b8] text-[14px] w-full">
        <p className="leading-[21px]">{text}</p>
      </div>
    </div>
  );
}
type ContainerTextProps = {
  text: string;
  additionalClassNames?: string;
};

function ContainerText({ text, additionalClassNames = "" }: ContainerTextProps) {
  return (
    <div className={clsx("content-stretch flex flex-col items-start relative shrink-0 w-full", additionalClassNames)}>
      <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium justify-center leading-[0] not-italic relative shrink-0 text-[#e2e8f0] text-[16px] w-full">
        <p className="leading-[24px]">{text}</p>
      </div>
    </div>
  );
}

export default function IptvHomeScreen() {
  return (
    <div className="content-stretch flex flex-col items-start relative size-full" data-name="IPTV Home Screen">
      <div className="bg-[#101c22] content-stretch flex flex-col items-start min-h-[896px] overflow-clip relative shrink-0 w-full" data-name="Background">
        <div className="h-[896px] relative shrink-0 w-full" data-name="Container">
          <div className="absolute content-stretch flex flex-col items-start left-0 pb-[8px] pt-[16px] px-[16px] right-0 top-[72px]" data-name="Heading 3 - Section Header: Continue Watching">
            <div className="flex flex-col font-['Inter:Bold',sans-serif] font-bold h-[23px] justify-center leading-[0] not-italic relative shrink-0 text-[18px] text-white tracking-[-0.27px] w-[163.61px]">
              <p className="leading-[22.5px]">Continue Watching</p>
            </div>
          </div>
          <div className="absolute h-[188px] left-0 overflow-clip right-0 top-[118.5px]" data-name="Carousel: Continue Watching">
            <div className="absolute bottom-0 content-stretch flex gap-[12px] items-start left-0 px-[16px] top-0" data-name="Container">
              <div className="content-stretch flex flex-col gap-[8px] h-full items-start relative rounded-[8px] shrink-0 w-[240px]" data-name="Container">
                <div className="content-stretch flex flex-col items-start justify-end overflow-clip pt-[113px] relative rounded-[8px] shrink-0 w-full" data-name="Background">
                  <div className="absolute inset-0 overflow-hidden pointer-events-none rounded-[8px]">
                    <img alt="" className="absolute h-[177.78%] left-0 max-w-none top-[-38.89%] w-full" src={imgBackground} />
                  </div>
                  <Container1>
                    <div className="bg-[rgba(30,41,59,0.5)] h-[6px] relative rounded-[9999px] shrink-0 w-full" data-name="Overlay">
                      <div className="absolute bg-[#2badee] h-[6px] left-0 right-[33.33%] rounded-[9999px] top-0" data-name="Background" />
                    </div>
                  </Container1>
                </div>
                <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Container">
                  <ContainerText text="BBC News" />
                  <ContainerText1 text="24m left" />
                </div>
              </div>
              <div className="content-stretch flex flex-col gap-[8px] h-full items-start relative rounded-[8px] shrink-0 w-[240px]" data-name="Container">
                <div className="content-stretch flex flex-col items-start justify-end overflow-clip pt-[113px] relative rounded-[8px] shrink-0 w-full" data-name="Background">
                  <div className="absolute inset-0 overflow-hidden pointer-events-none rounded-[8px]">
                    <img alt="" className="absolute h-[177.78%] left-0 max-w-none top-[-38.89%] w-full" src={imgBackground1} />
                  </div>
                  <Container1>
                    <div className="bg-[rgba(30,41,59,0.5)] h-[6px] relative rounded-[9999px] shrink-0 w-full" data-name="Overlay">
                      <div className="absolute bg-[#2badee] h-[6px] left-0 right-3/4 rounded-[9999px] top-0" data-name="Background" />
                    </div>
                  </Container1>
                </div>
                <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Container">
                  <ContainerText text="ESPN" />
                  <ContainerText1 text="1h 15m left" />
                </div>
              </div>
            </div>
          </div>
          <div className="absolute content-stretch flex flex-col items-start left-0 pb-[8px] pt-[24px] px-[16px] right-0 top-[306.5px]" data-name="Heading 3 - Section Header: Favorites">
            <div className="flex flex-col font-['Inter:Bold',sans-serif] font-bold h-[23px] justify-center leading-[0] not-italic relative shrink-0 text-[18px] text-white tracking-[-0.27px] w-[79px]">
              <p className="leading-[22.5px]">Favorites</p>
            </div>
          </div>
          <div className="absolute h-[192px] left-0 overflow-clip right-0 top-[361px]" data-name="Carousel: Favorites">
            <div className="absolute bottom-0 content-stretch flex gap-[12px] items-start left-0 px-[16px] top-0" data-name="Container">
              <div className="content-stretch flex flex-col gap-[8px] h-full items-start relative rounded-[8px] shrink-0 w-[160px]" data-name="Container">
                <Background>
                  <img alt="" className="absolute left-0 max-w-none size-full top-0" src={imgBackground2} />
                </Background>
                <ContainerText text="HBO" additionalClassNames="overflow-clip" />
              </div>
              <div className="content-stretch flex flex-col gap-[8px] h-full items-start relative rounded-[8px] shrink-0 w-[160px]" data-name="Container">
                <Background>
                  <img alt="" className="absolute left-0 max-w-none size-full top-0" src={imgBackground3} />
                </Background>
                <ContainerText text="ESPN" additionalClassNames="overflow-clip" />
              </div>
              <div className="content-stretch flex flex-col gap-[8px] h-full items-start relative rounded-[8px] shrink-0 w-[160px]" data-name="Container">
                <Background>
                  <img alt="" className="absolute left-0 max-w-none size-full top-0" src={imgBackground4} />
                </Background>
                <ContainerText text="BBC World News" additionalClassNames="overflow-clip" />
              </div>
              <div className="content-stretch flex flex-col gap-[8px] h-full items-start relative rounded-[8px] shrink-0 w-[160px]" data-name="Container">
                <Background>
                  <img alt="" className="absolute left-0 max-w-none size-full top-0" src={imgBackground5} />
                </Background>
                <ContainerText text="Cinema Max" additionalClassNames="overflow-clip" />
              </div>
            </div>
          </div>
          <div className="absolute content-stretch flex flex-col items-start left-0 pb-[8px] pt-[24px] px-[16px] right-0 top-[553px]" data-name="Heading 3 - Section Header: Sports">
            <div className="flex flex-col font-['Inter:Bold',sans-serif] font-bold h-[23px] justify-center leading-[0] not-italic relative shrink-0 text-[18px] text-white tracking-[-0.27px] w-[56.78px]">
              <p className="leading-[22.5px]">Sports</p>
            </div>
          </div>
          <div className="absolute h-[192px] left-0 overflow-clip right-0 top-[607.5px]" data-name="Carousel: Sports">
            <div className="absolute bottom-0 content-stretch flex gap-[12px] items-start left-0 px-[16px] top-0" data-name="Container">
              <div className="content-stretch flex flex-col gap-[8px] h-full items-start relative rounded-[8px] shrink-0 w-[160px]" data-name="Container">
                <Background>
                  <img alt="" className="absolute left-0 max-w-none size-full top-0" src={imgBackground6} />
                </Background>
                <ContainerText text="Fox Sports 1" additionalClassNames="overflow-clip" />
              </div>
              <div className="content-stretch flex flex-col gap-[8px] h-full items-start relative rounded-[8px] shrink-0 w-[160px]" data-name="Container">
                <Background>
                  <img alt="" className="absolute left-0 max-w-none size-full top-0" src={imgBackground7} />
                </Background>
                <ContainerText text="Tennis Channel" additionalClassNames="overflow-clip" />
              </div>
              <div className="content-stretch flex flex-col gap-[8px] h-full items-start relative rounded-[8px] shrink-0 w-[160px]" data-name="Container">
                <Background>
                  <img alt="" className="absolute left-0 max-w-none size-full top-0" src={imgBackground8} />
                </Background>
                <ContainerText text="Fight TV" additionalClassNames="overflow-clip" />
              </div>
              <div className="content-stretch flex flex-col gap-[8px] h-full items-start relative rounded-[8px] shrink-0 w-[160px]" data-name="Container">
                <Background>
                  <img alt="" className="absolute left-0 max-w-none size-full top-0" src={imgBackground9} />
                </Background>
                <ContainerText text="Golf Channel" additionalClassNames="overflow-clip" />
              </div>
            </div>
          </div>
          <div className="absolute backdrop-blur-[2px] bg-[rgba(16,28,34,0.8)] content-stretch flex items-center justify-between left-0 p-[16px] right-0 top-0" data-name="Top App Bar">
            <div className="content-stretch flex gap-[4px] items-center relative shrink-0" data-name="Container">
              <div className="content-stretch flex flex-col items-start relative shrink-0" data-name="Heading 2">
                <div className="flex flex-col font-['Inter:Bold',sans-serif] font-bold h-[23px] justify-center leading-[0] not-italic relative shrink-0 text-[18px] text-white tracking-[-0.27px] w-[93.08px]">
                  <p className="leading-[22.5px]">My Playlist</p>
                </div>
              </div>
              <div className="h-[7.4px] relative shrink-0 w-[12px]" data-name="Container">
                <svg className="absolute block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 12 7.4">
                  <g id="Container">
                    <path d={svgPaths.p1adfde00} fill="var(--fill-0, #94A3B8)" id="Icon" />
                  </g>
                </svg>
              </div>
            </div>
            <div className="content-stretch flex gap-[8px] items-center relative shrink-0" data-name="Container">
              <div className="content-stretch flex items-center justify-center relative rounded-[9999px] shrink-0 size-[40px]" data-name="Button">
                <div className="relative shrink-0 size-[18px]" data-name="Container">
                  <svg className="absolute block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 18 18">
                    <g id="Container">
                      <path d={svgPaths.p8a35e00} fill="var(--fill-0, #CBD5E1)" id="Icon" />
                    </g>
                  </svg>
                </div>
              </div>
              <div className="content-stretch flex items-center justify-center relative rounded-[9999px] shrink-0 size-[40px]" data-name="Button">
                <Container>
                  <path d={svgPaths.p3cdadd00} fill="var(--fill-0, #CBD5E1)" id="Icon" />
                </Container>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div className="absolute backdrop-blur-[2px] bg-[rgba(16,28,34,0.8)] bottom-0 content-stretch flex flex-col items-start left-0 pt-px right-0" data-name="Bottom Navigation Bar">
        <div aria-hidden="true" className="absolute border-[rgba(30,41,59,0.5)] border-solid border-t inset-0 pointer-events-none" />
        <div className="h-[80px] relative shrink-0 w-full" data-name="Container">
          <div className="flex flex-row items-center size-full">
            <div className="bg-clip-padding border-0 border-[transparent] border-solid content-stretch flex items-center px-[8px] relative size-full">
              <div className="content-stretch flex flex-[1_0_0] flex-col gap-[4px] items-center justify-center min-h-px min-w-px relative" data-name="Link">
                <div className="h-[18px] relative shrink-0 w-[16px]" data-name="Container">
                  <svg className="absolute block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 16 18">
                    <g id="Container">
                      <path d={svgPaths.p12a32500} fill="var(--fill-0, #2BADEE)" id="Icon" />
                    </g>
                  </svg>
                </div>
                <div className="content-stretch flex flex-col items-start relative shrink-0" data-name="Container">
                  <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[16px] justify-center leading-[0] not-italic relative shrink-0 text-[#2badee] text-[12px] w-[33.89px]">
                    <p className="leading-[16px]">Home</p>
                  </div>
                </div>
              </div>
              <div className="content-stretch flex flex-[1_0_0] flex-col gap-[4px] items-center justify-center min-h-px min-w-px relative" data-name="Link">
                <div className="h-[18.35px] relative shrink-0 w-[20px]" data-name="Container">
                  <svg className="absolute block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 20 18.35">
                    <g id="Container">
                      <path d={svgPaths.p279a9400} fill="var(--fill-0, #94A3B8)" id="Icon" />
                    </g>
                  </svg>
                </div>
                <div className="content-stretch flex flex-col items-start relative shrink-0" data-name="Container">
                  <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[16px] justify-center leading-[0] not-italic relative shrink-0 text-[#94a3b8] text-[12px] w-[52.61px]">
                    <p className="leading-[16px]">Favorites</p>
                  </div>
                </div>
              </div>
              <div className="content-stretch flex flex-[1_0_0] flex-col gap-[4px] items-center justify-center min-h-px min-w-px relative" data-name="Link">
                <div className="h-[20px] relative shrink-0 w-[19px]" data-name="Container">
                  <svg className="absolute block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 19 20">
                    <g id="Container">
                      <path d={svgPaths.p23f03200} fill="var(--fill-0, #94A3B8)" id="Icon" />
                    </g>
                  </svg>
                </div>
                <div className="content-stretch flex flex-col items-start relative shrink-0" data-name="Container">
                  <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[16px] justify-center leading-[0] not-italic relative shrink-0 text-[#94a3b8] text-[12px] w-[62.59px]">
                    <p className="leading-[16px]">Categories</p>
                  </div>
                </div>
              </div>
              <div className="content-stretch flex flex-[1_0_0] flex-col gap-[4px] items-center justify-center min-h-px min-w-px relative" data-name="Link">
                <Container>
                  <path d={svgPaths.p3cdadd00} fill="var(--fill-0, #94A3B8)" id="Icon" />
                </Container>
                <div className="content-stretch flex flex-col items-start relative shrink-0" data-name="Container">
                  <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[16px] justify-center leading-[0] not-italic relative shrink-0 text-[#94a3b8] text-[12px] w-[47.08px]">
                    <p className="leading-[16px]">Settings</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}