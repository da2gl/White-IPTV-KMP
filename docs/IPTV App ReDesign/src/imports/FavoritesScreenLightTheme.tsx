import svgPaths from "./svg-fbr6gopo4m";
import imgImage from "figma:asset/57418cfa2463cccea07e0de8800fb22fda3f5776.png";
import imgImage1 from "figma:asset/dfe784cdf1a8c2fe542da99cb728291d24dc1aa2.png";
import imgImage2 from "figma:asset/a5fd0f6b92255373453b7b4e768a4f4bdf242dfe.png";
import imgImage3 from "figma:asset/591c7fa50028bcc883a5f19c4cf5364c6d6feac7.png";
import imgImage4 from "figma:asset/13d042717dfda18fa9f48cfb2c9b9337f40bdaa4.png";
import imgImage5 from "figma:asset/f3e2ace70b83bf4b32a4eb04ae21de459c8838ab.png";

function Link({ children }: React.PropsWithChildren<{}>) {
  return (
    <div className="flex-[1_0_0] min-h-px min-w-px relative self-stretch">
      <div className="bg-clip-padding border-0 border-[transparent] border-solid content-stretch flex flex-col gap-[4px] items-center justify-end relative size-full">{children}</div>
    </div>
  );
}

function Image({ children }: React.PropsWithChildren<{}>) {
  return (
    <div className="aspect-square relative rounded-[24px] shrink-0 w-full">
      <div className="absolute inset-0 overflow-hidden pointer-events-none rounded-[24px]">{children}</div>
    </div>
  );
}

function Container1({ children }: React.PropsWithChildren<{}>) {
  return (
    <div className="h-[32px] relative shrink-0 w-[20px]">
      <svg className="absolute block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 20 32">
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
      <div className="flex flex-col font-['Inter:Regular',sans-serif] font-normal justify-center leading-[0] not-italic relative shrink-0 text-[#64748b] text-[14px] w-full">
        <p className="leading-[20px]">{text}</p>
      </div>
    </div>
  );
}
type ContainerTextProps = {
  text: string;
};

function ContainerText({ text }: ContainerTextProps) {
  return (
    <div className="content-stretch flex flex-col items-start relative shrink-0 w-full">
      <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium justify-center leading-[0] not-italic relative shrink-0 text-[#0f172a] text-[16px] w-full">
        <p className="leading-[24px]">{text}</p>
      </div>
    </div>
  );
}

function Container() {
  return (
    <div className="h-[14.25px] relative shrink-0 w-[15px]">
      <svg className="absolute block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 15 14.25">
        <g id="Container">
          <path d={svgPaths.p1755bb80} fill="var(--fill-0, #FACC15)" id="Icon" />
        </g>
      </svg>
    </div>
  );
}

export default function FavoritesScreenLightTheme() {
  return (
    <div className="bg-[#f6f7f8] relative size-full" data-name="Favorites Screen (Light Theme)">
      <div className="absolute h-[739px] left-0 right-0 top-[124px]" data-name="Main → Image Grid">
        <div className="absolute content-stretch flex flex-col gap-[10px] inset-[16px_203px_498px_16px] items-start" data-name="Container">
          <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Container">
            <Image>
              <img alt="" className="absolute left-0 max-w-none size-full top-0" src={imgImage} />
            </Image>
            <div className="absolute backdrop-blur-[2px] bg-[rgba(0,0,0,0.3)] content-stretch flex items-center justify-center right-[8px] rounded-[9999px] size-[32px] top-[8px]" data-name="Button">
              <Container />
            </div>
          </div>
          <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Container">
            <ContainerText text="Channel One" />
            <ContainerText1 text="From: Main" />
          </div>
        </div>
        <div className="absolute content-stretch flex flex-col gap-[10px] inset-[16px_16px_498px_203px] items-start" data-name="Container">
          <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Container">
            <Image>
              <img alt="" className="absolute left-0 max-w-none size-full top-0" src={imgImage1} />
            </Image>
            <div className="absolute backdrop-blur-[2px] bg-[rgba(0,0,0,0.3)] content-stretch flex items-center justify-center right-[8px] rounded-[9999px] size-[32px] top-[8px]" data-name="Button">
              <Container />
            </div>
          </div>
          <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Container">
            <ContainerText text="Sports HD" />
            <ContainerText1 text="From: Sports" />
          </div>
        </div>
        <div className="absolute content-stretch flex flex-col gap-[10px] inset-[257px_203px_257px_16px] items-start" data-name="Container">
          <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Container">
            <Image>
              <img alt="" className="absolute left-0 max-w-none size-full top-0" src={imgImage2} />
            </Image>
            <div className="absolute backdrop-blur-[2px] bg-[rgba(0,0,0,0.3)] content-stretch flex items-center justify-center right-[8px] rounded-[9999px] size-[32px] top-[8px]" data-name="Button">
              <Container />
            </div>
          </div>
          <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Container">
            <ContainerText text="Movie Zone" />
            <ContainerText1 text="From: Main" />
          </div>
        </div>
        <div className="absolute content-stretch flex flex-col gap-[10px] inset-[257px_16px_257px_203px] items-start" data-name="Container">
          <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Container">
            <Image>
              <img alt="" className="absolute left-0 max-w-none size-full top-0" src={imgImage3} />
            </Image>
            <div className="absolute backdrop-blur-[2px] bg-[rgba(0,0,0,0.3)] content-stretch flex items-center justify-center right-[8px] rounded-[9999px] size-[32px] top-[8px]" data-name="Button">
              <Container />
            </div>
          </div>
          <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Container">
            <ContainerText text="Kids Central" />
            <ContainerText1 text="From: Kids" />
          </div>
        </div>
        <div className="absolute content-stretch flex flex-col gap-[10px] inset-[498px_203px_16px_16px] items-start" data-name="Container">
          <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Container">
            <Image>
              <img alt="" className="absolute left-0 max-w-none size-full top-0" src={imgImage4} />
            </Image>
            <div className="absolute backdrop-blur-[2px] bg-[rgba(0,0,0,0.3)] content-stretch flex items-center justify-center right-[8px] rounded-[9999px] size-[32px] top-[8px]" data-name="Button">
              <Container />
            </div>
          </div>
          <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Container">
            <ContainerText text="News Now" />
            <ContainerText1 text="From: Main" />
          </div>
        </div>
        <div className="absolute content-stretch flex flex-col gap-[10px] inset-[498px_16px_16px_203px] items-start" data-name="Container">
          <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Container">
            <Image>
              <img alt="" className="absolute left-0 max-w-none size-full top-0" src={imgImage5} />
            </Image>
            <div className="absolute backdrop-blur-[2px] bg-[rgba(0,0,0,0.3)] content-stretch flex items-center justify-center right-[8px] rounded-[9999px] size-[32px] top-[8px]" data-name="Button">
              <Container />
            </div>
          </div>
          <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Container">
            <ContainerText text="Docu World" />
            <ContainerText1 text="From: Documentaries" />
          </div>
        </div>
      </div>
      <div className="absolute backdrop-blur-[2px] bg-[rgba(246,247,248,0.8)] content-stretch flex flex-col items-start left-0 right-0 top-0" data-name="Header - Top App Bar">
        <div className="relative shrink-0 w-full" data-name="Container">
          <div className="flex flex-row items-center size-full">
            <div className="content-stretch flex items-center p-[16px] relative w-full">
              <div className="content-stretch flex flex-[1_0_0] flex-col items-start min-h-px min-w-px relative" data-name="Heading 1">
                <div className="flex flex-col font-['Inter:Bold',sans-serif] font-bold justify-center leading-[0] not-italic relative shrink-0 text-[#0f172a] text-[20px] w-full">
                  <p className="leading-[28px]">⭐ Favorites</p>
                </div>
              </div>
              <div className="content-stretch flex items-center justify-center overflow-clip relative rounded-[9999px] shrink-0 size-[40px]" data-name="Button">
                <div className="relative shrink-0 size-[18px]" data-name="Container">
                  <svg className="absolute block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 18 18">
                    <g id="Container">
                      <path d={svgPaths.p8a35e00} fill="var(--fill-0, #0F172A)" id="Icon" />
                    </g>
                  </svg>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div className="relative shrink-0 w-full" data-name="Chips / Filters">
          <div className="overflow-clip rounded-[inherit] size-full">
            <div className="content-stretch flex gap-[8px] items-start pb-[16px] px-[16px] relative w-full">
              <div className="bg-[#2badee] content-stretch flex gap-[8px] h-[36px] items-center justify-center pl-[16px] pr-[12px] relative rounded-[9999px] shrink-0" data-name="Button">
                <div className="content-stretch flex flex-col items-center relative shrink-0" data-name="Container">
                  <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[21px] justify-center leading-[0] not-italic relative shrink-0 text-[14px] text-center text-white w-[72.67px]">
                    <p className="leading-[21px]">Playlist: All</p>
                  </div>
                </div>
                <div className="h-[6.167px] relative shrink-0 w-[10px]" data-name="Container">
                  <svg className="absolute block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 10 6.16667">
                    <g id="Container">
                      <path d={svgPaths.p3b35c180} fill="var(--fill-0, white)" id="Icon" />
                    </g>
                  </svg>
                </div>
              </div>
              <div className="bg-[#e2e8f0] content-stretch flex h-[36px] items-center justify-center pl-[16px] pr-[12px] relative rounded-[9999px] shrink-0" data-name="Button">
                <div className="content-stretch flex flex-col items-center relative shrink-0" data-name="Container">
                  <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[21px] justify-center leading-[0] not-italic relative shrink-0 text-[#1e293b] text-[14px] text-center w-[32.69px]">
                    <p className="leading-[21px]">Main</p>
                  </div>
                </div>
              </div>
              <div className="bg-[#e2e8f0] content-stretch flex h-[36px] items-center justify-center pl-[16px] pr-[12px] relative rounded-[9999px] shrink-0" data-name="Button">
                <div className="content-stretch flex flex-col items-center relative shrink-0" data-name="Container">
                  <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[21px] justify-center leading-[0] not-italic relative shrink-0 text-[#1e293b] text-[14px] text-center w-[29.53px]">
                    <p className="leading-[21px]">Kids</p>
                  </div>
                </div>
              </div>
              <div className="bg-[#e2e8f0] content-stretch flex h-[36px] items-center justify-center pl-[16px] pr-[12px] relative rounded-[9999px] shrink-0" data-name="Button">
                <div className="content-stretch flex flex-col items-center relative shrink-0" data-name="Container">
                  <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[21px] justify-center leading-[0] not-italic relative shrink-0 text-[#1e293b] text-[14px] text-center w-[43.98px]">
                    <p className="leading-[21px]">Sports</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div className="absolute backdrop-blur-[2px] bg-[rgba(246,247,248,0.9)] content-stretch flex h-[69px] items-start justify-center left-0 pb-[8px] pt-[9px] right-0 top-[863px]" data-name="Bottom Nav Bar">
        <div aria-hidden="true" className="absolute border-[#e2e8f0] border-solid border-t inset-0 pointer-events-none" />
        <Link>
          <div className="h-[32px] relative shrink-0 w-[16px]" data-name="Container">
            <svg className="absolute block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 16 32">
              <g id="Container">
                <path d={svgPaths.p9afca80} fill="var(--fill-0, #64748B)" id="Icon" />
              </g>
            </svg>
          </div>
          <div className="content-stretch flex flex-col items-start relative shrink-0" data-name="Container">
            <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[16px] justify-center leading-[0] not-italic relative shrink-0 text-[#64748b] text-[12px] w-[33.89px]">
              <p className="leading-[16px]">Home</p>
            </div>
          </div>
        </Link>
        <Link>
          <Container1>
            <path d={svgPaths.p3bc903c0} fill="var(--fill-0, #64748B)" id="Icon" />
          </Container1>
          <div className="content-stretch flex flex-col items-start relative shrink-0" data-name="Container">
            <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[16px] justify-center leading-[0] not-italic relative shrink-0 text-[#64748b] text-[12px] w-[43.08px]">
              <p className="leading-[16px]">Live TV</p>
            </div>
          </div>
        </Link>
        <Link>
          <Container1>
            <path d={svgPaths.p1e916cc0} fill="var(--fill-0, #2BADEE)" id="Icon" />
          </Container1>
          <div className="content-stretch flex flex-col items-start relative shrink-0" data-name="Container">
            <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[16px] justify-center leading-[0] not-italic relative shrink-0 text-[#2badee] text-[12px] w-[52.61px]">
              <p className="leading-[16px]">Favorites</p>
            </div>
          </div>
        </Link>
        <Link>
          <div className="h-[32px] relative shrink-0 w-[20.1px]" data-name="Container">
            <svg className="absolute block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 20.1 32">
              <g id="Container">
                <path d={svgPaths.p175f2a00} fill="var(--fill-0, #64748B)" id="Icon" />
              </g>
            </svg>
          </div>
          <div className="content-stretch flex flex-col items-start relative shrink-0" data-name="Container">
            <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[16px] justify-center leading-[0] not-italic relative shrink-0 text-[#64748b] text-[12px] w-[47.08px]">
              <p className="leading-[16px]">Settings</p>
            </div>
          </div>
        </Link>
      </div>
    </div>
  );
}